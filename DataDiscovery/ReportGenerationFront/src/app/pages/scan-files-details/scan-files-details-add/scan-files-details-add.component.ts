import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { finalize, Subject, takeUntil } from 'rxjs';
import { PiiScanRequest } from '../../../core/models/PiiScanRequest';
import { Router } from '@angular/router';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import editorConfig from '../../../../shared/editor-config';
import { ReportDetails } from '../../../core/models/ReportDetails';
import { GlobalService } from '../../../core/services/global-service.service';
import { ReportDetailsService } from '../../../core/services/report-details.service';
import { SharedModule } from '../../../../shared/shared.module';
import { FilesScanService } from '../../../core/services/files-scan.service';
import { RemotePiiScanRequest } from '../../../core/models/RemotePiiScanRequest';
import { HostsService } from '../../../core/services/hosts.service';
import { Host } from '../../../core/models/Host';

@Component({
  selector: 'app-scan-files-details-add',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './scan-files-details-add.component.html',
  styleUrl: './scan-files-details-add.component.css',
})
export class ScanFilesDetailsAddComponent implements OnInit, OnDestroy {
  isLoading: boolean;
  modalRef: NgbModalRef;
  showError: boolean;
  errorMsg: string;
  public destroy$: Subject<boolean>;
  isEdit: boolean;
  idToEdit: number;
  remotePiiScanRequestForm: FormGroup;
  remotePiiScanRequestObj: RemotePiiScanRequest;
  dropdownSettings = {};
  piiPatternDropdown = [];
  boxVisibility: boolean = true;
  active = 1;
  hostsList: Host[];
  configuredHosts: string[] = [];
  nonConfiguredHosts: string[] = [];

  pageSize = 4;
  currentPage = 1;

  selectedServerType: 'configured' | 'non-configured' = 'configured';
  manualIpList: string = '';

  constructor(
    private router: Router,
    private fb: FormBuilder,
    public globalService: GlobalService,
    private spinner: NgxUiLoaderService,
    private modalService: NgbModal,
    public filesScanService: FilesScanService,
    public hostsService: HostsService,
    private cdr: ChangeDetectorRef
  ) {
    this.isLoading = false;
    this.showError = false;
    this.errorMsg = '';
    this.destroy$ = new Subject<boolean>();
    this.isEdit = false;
    this.idToEdit = 0;
    this.hostsList = [];
    this.remotePiiScanRequestForm = new FormGroup({});
    this.remotePiiScanRequestObj = {} as RemotePiiScanRequest;
    this.piiPatternDropdown = [
      { id: 1, itemName: 'pan' },
      { id: 2, itemName: 'aadhaar' },
      { id: 3, itemName: 'voter' },
      { id: 4, itemName: 'dl' },
      { id: 5, itemName: 'passport' },
      { id: 6, itemName: 'ifsc' },
      { id: 7, itemName: 'micr' },
      { id: 8, itemName: 'account_number' },
      { id: 9, itemName: 'cif_number' },
      { id: 10, itemName: 'debit_card' },
      { id: 11, itemName: 'credit_card' },
      { id: 12, itemName: 'ckyc' },
      { id: 13, itemName: 'loan_account' },
      { id: 14, itemName: 'fd_account' },
      { id: 15, itemName: 'customer_id' },
      { id: 16, itemName: 'demat_account' },
      { id: 17, itemName: 'email' },
      { id: 18, itemName: 'phone' },
      { id: 19, itemName: 'vehicle_number' },
    ];
  }

  ngOnInit(): void {
    this.dropdownSettings = {
      singleSelection: false,
      primaryKey: 'id',
      labelKey: 'itemName',
      text: 'Select Pii Types',
      selectAllText: 'Select All',
      unSelectAllText: 'UnSelect All',
      disabled: this.isEdit,
      itemsShowLimit: 5,
      defaultOpen: false,
      enableSearchFilter: true,
      noDataAvailablePlaceholderText: 'There is no item availabale to show',
    };
    this.buildForm();
    this.remotePiiScanRequestForm
      .get('password')
      ?.valueChanges.subscribe(() => {
        this.remotePiiScanRequestForm.updateValueAndValidity();
      });
    this.remotePiiScanRequestForm
      .get('conPassword')
      ?.valueChanges.subscribe(() => {
        this.remotePiiScanRequestForm.updateValueAndValidity();
      });
    this.getAllActieHostList();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  cancel() {
    this.router.navigate(['/jrm/scan-files-details/files_list']);
  }

  toggleBox() {
    this.boxVisibility = !this.boxVisibility;
  }

buildForm(): void {
  const defaultServerType = this.remotePiiScanRequestObj?.serverType || 'linux';
  const defaultPort = this.getDefaultPort(defaultServerType);

  this.remotePiiScanRequestForm = this.fb.group(
    {
      maxFileSize: [
        this.remotePiiScanRequestObj?.maxFileSize,
        [Validators.required, Validators.min(0), Validators.max(10)],
      ],
      targetName: [
        this.remotePiiScanRequestObj?.targetName,
        [Validators.required, Validators.minLength(2)],
      ],
      serverType: [
        defaultServerType,
        Validators.required,
      ],
      filePath: [
        this.remotePiiScanRequestObj?.filePath,
        [Validators.required, Validators.minLength(1)],
      ],
      piiTypes: [
        this.remotePiiScanRequestObj?.piiTypes,
        [Validators.minLength(2)],
      ],
      excludePatterns: [
        this.remotePiiScanRequestObj?.excludePatterns,
        [Validators.minLength(2)],
      ],
      stopScannAfter: [
        this.remotePiiScanRequestObj?.stopScannAfter,
        [Validators.minLength(2)],
      ],
      host: [
        this.remotePiiScanRequestObj?.connection?.host ?? [],
        [Validators.required],
      ],
      port: [
        defaultPort,
        [Validators.required, Validators.min(1)],
      ],
      username: [
        this.remotePiiScanRequestObj?.connection?.username ?? '',
        [Validators.required],
      ],
      password: [
        this.remotePiiScanRequestObj?.connection?.password ?? '',
        [Validators.required],
      ],
      conPassword: [
        this.remotePiiScanRequestObj?.connection?.conPassword ?? '',
        [Validators.required],
      ],
    },
    {
      validators: this.passwordMatchValidator.bind(this),
    }
  );

  this.remotePiiScanRequestForm.get('serverType')?.valueChanges.subscribe((type: string) => {
    const portControl = this.remotePiiScanRequestForm.get('port');
    if (portControl && (portControl.pristine || !portControl.dirty)) {
      const newPort = type === 'windows' ? 5985 : 22;
      portControl.setValue(newPort);
    }
  });
}


  passwordMatchValidator(
    group: AbstractControl
  ): { [key: string]: any } | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('conPassword')?.value;

    if (password !== confirmPassword) {
      group.get('conPassword')?.setErrors({ passwordMismatch: true });
    } else {
      const errors = group.get('conPassword')?.errors;
      if (errors && errors['passwordMismatch']) {
        delete errors['passwordMismatch'];
        if (Object.keys(errors).length === 0) {
          group.get('conPassword')?.setErrors(null);
        } else {
          group.get('conPassword')?.setErrors(errors);
        }
      }
    }
    return null;
  }


  getDefaultPort(serverType: string): number {
  switch (serverType?.toLowerCase()) {
    case 'windows':
      return 5985;
    case 'linux':
    default:
      return 22;
  }
}

  getErrorMessage(fieldName: string, fieldLabel: string): string {
    return this.globalService.getErrorMessage(
      this.remotePiiScanRequestForm,
      fieldName,
      fieldLabel
    );
  }

  changeTab(direction: 'next' | 'prev'): void {
    const totalTabs = 5;

    if (direction === 'next' && this.active < totalTabs) {
      this.active++;
    } else if (direction === 'prev' && this.active > 1) {
      this.active--;
    }
  }

  getAllActieHostList(): void {
    this.isLoading = true;
    this.spinner.start();
    this.hostsService
      .getAllActiveHostsList()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
          this.spinner.stop();
        })
      )
      .subscribe({
        next: (data) => {
          this.hostsList = data;
          this.cdr.detectChanges();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  // onHostCheckboxChange(event: Event): void {
  //   const checkbox = event.target as HTMLInputElement;
  //   const hostName = checkbox.value;
  //   const selectedHosts =
  //     this.remotePiiScanRequestForm.get('host')?.value || [];

  //   if (checkbox.checked) {
  //     if (!selectedHosts.includes(hostName)) {
  //       selectedHosts.push(hostName);
  //     }
  //   } else {
  //     const index = selectedHosts.indexOf(hostName);
  //     if (index > -1) {
  //       selectedHosts.splice(index, 1);
  //     }
  //   }

  //   this.remotePiiScanRequestForm.get('host')?.setValue([...selectedHosts]);
  //   this.remotePiiScanRequestForm.get('host')?.updateValueAndValidity();
  // }

  onHostCheckboxChange(event: any): void {
    const ip = event.target.value;

    if (event.target.checked) {
      if (!this.configuredHosts.includes(ip)) {
        this.configuredHosts.push(ip);
      }
    } else {
      this.configuredHosts = this.configuredHosts.filter((host) => host !== ip);
    }

    // Update the actual 'host' control in the form (not 'configuredHosts')
    const hostControl = this.remotePiiScanRequestForm.get('host');
    if (hostControl) {
      hostControl.setValue(this.configuredHosts);
      hostControl.markAsTouched();
      hostControl.updateValueAndValidity();
    }
  }

  onServerTypeChange(value: 'configured' | 'non-configured') {
    this.selectedServerType = value;

    if (value === 'configured') {
      this.manualIpList = '';

      // Remove manual IP control if it exists
      if (this.remotePiiScanRequestForm.contains('manualIps')) {
        this.remotePiiScanRequestForm.removeControl('manualIps');
      }

      // Ensure host control is ready
      if (!this.remotePiiScanRequestForm.contains('host')) {
        this.remotePiiScanRequestForm.addControl(
          'host',
          new FormControl([], Validators.required)
        );
      }

      // Reset selected configured hosts
      this.configuredHosts = [];
      this.remotePiiScanRequestForm.get('host')?.setValue([]);
    } else {
      this.configuredHosts = [];

      // Remove host control
      if (this.remotePiiScanRequestForm.contains('host')) {
        this.remotePiiScanRequestForm.removeControl('host');
      }

      // Add manual IP control
      if (!this.remotePiiScanRequestForm.contains('manualIps')) {
        this.remotePiiScanRequestForm.addControl(
          'manualIps',
          new FormControl('', Validators.required)
        );
      }

      this.remotePiiScanRequestForm
        .get('manualIps')
        ?.setValue(this.manualIpList);
    }
  }

  get paginatedHosts() {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.hostsList.slice(start, start + this.pageSize);
  }

  nextPage() {
    if (this.currentPage * this.pageSize < this.hostsList.length) {
      this.currentPage++;
    }
  }

  prevPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  isValidIp(ip: string): boolean {
    const ipRegex =
      /^(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)){3}$/;
    return ipRegex.test(ip);
  }

  getManualIps(): string[] {
    return this.manualIpList
      .split('\n')
      .map((ip) => ip.trim())
      .filter((ip) => ip && this.isValidIp(ip));
  }

  // onServerTypeChange(value: 'configured' | 'non-configured') {
  //   this.selectedServerType = value;

  //   if (value === 'configured') {
  //     this.manualIpList = '';

  //     // Remove manual IP control if it exists
  //     if (this.remotePiiScanRequestForm.contains('manualIps')) {
  //       this.remotePiiScanRequestForm.removeControl('manualIps');
  //     }

  //     // Add configuredHosts control if not present
  //     if (!this.remotePiiScanRequestForm.contains('configuredHosts')) {
  //       this.remotePiiScanRequestForm.addControl(
  //         'configuredHosts',
  //         new FormControl([], Validators.required)
  //       );
  //     }

  //     // Always reset the form control to an empty array
  //     this.configuredHosts = [];
  //     this.remotePiiScanRequestForm.get('configuredHosts')?.setValue([]);
  //   } else {
  //     this.configuredHosts = [];

  //     // Remove configuredHosts control
  //     if (this.remotePiiScanRequestForm.contains('configuredHosts')) {
  //       this.remotePiiScanRequestForm.removeControl('configuredHosts');
  //     }

  //     // Add back manualIps control
  //     if (!this.remotePiiScanRequestForm.contains('manualIps')) {
  //       this.remotePiiScanRequestForm.addControl(
  //         'manualIps',
  //         new FormControl('', Validators.required)
  //       );
  //     }

  //     this.remotePiiScanRequestForm
  //       .get('manualIps')
  //       ?.setValue(this.manualIpList);
  //   }
  // }

  submit() {
    this.showError = false;
    this.remotePiiScanRequestObj = {} as RemotePiiScanRequest;
    this.errorMsg = '';

    if (
      this.selectedServerType === 'configured' &&
      !this.remotePiiScanRequestForm.valid
    ) {
      this.globalService.showAlert(
        'Error',
        'Please enter all mandatory fields!'
      );
      return;
    }

    if (this.selectedServerType === 'non-configured') {
      const manualIps = this.getManualIps();
      if (manualIps.length === 0) {
        this.globalService.showAlert(
          'Error',
          'Please enter at least one valid IP address!'
        );
        return;
      }
    }

    const form = this.remotePiiScanRequestForm.value;

    this.remotePiiScanRequestObj = {
      targetName: form.targetName,
      maxFileSize: form.maxFileSize,
      stopScannAfter: form.stopScannAfter,
      filePath: form.filePath,
      piiTypes: (form.piiTypes || []).map((item: any) => item.itemName),
      excludePatterns: form.excludePatterns,
      serverType:form.serverType,
      createdById: this.globalService.getUserId(),
      connection: {
        host:
          this.selectedServerType === 'configured'
            ? form.host
            : this.getManualIps(),
        port: form.port || 22,
        username: form.username,
        password: form.password,
      },
    };

    this.isLoading = true;
    this.spinner.start();

    this.filesScanService
      .addRemoteScanData(this.remotePiiScanRequestObj)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
          this.spinner.stop();
        })
      )
      .subscribe({
        next: () => {
          this.globalService.showAlert('success', 'Successfully Submitted!');
          this.router.navigate(['/jrm/scan-files-details/files_list']);
        },
        error: (e) => {
          let msg: string;
          if (e.error.errorCode == 409) {
            msg = e.error.errorMessage;
            this.globalService.showAlert('error', msg);
          } else if (e.error.errorCode == 'INVALID_PATH') {
            msg = e.error.errorMessage;
            this.globalService.showAlert('error', msg);
            this.router.navigate(['/jrm/scan-files-details/files_list']);
          } else if (e.status == 500) {
            msg = e.error.errorMessage;
            this.globalService.showAlert('error', msg);
          }
        },
      });
  }
}
// submit() {
//   this.showError = false;
//   this.remotePiiScanRequestObj = {} as RemotePiiScanRequest;
//   this.errorMsg = '';
//   if (!this.remotePiiScanRequestForm.valid) {
//     this.globalService.showAlert(
//       'Error',
//       'Please enter all mandatory fields !'
//     );
//     return;
//   }
// const form = this.remotePiiScanRequestForm.value;

// this.remotePiiScanRequestObj = {
//   targetName: form.targetName,
//   maxFileSize: form.maxFileSize,
//   stopScannAfter: form.stopScannAfter,
//   filePath: form.filePath,
//   piiTypes: (form.piiTypes || []).map((item: any) => item.itemName),
//   excludePatterns: form.excludePatterns,
//   connection: {
//     host: form.host || [],
//     port: form.port || 22,
//     username: form.username,
//     password: form.password,
//     // conPassword: form.conPassword,
//   },
// };

//   this.isLoading = true;
//   this.spinner.start();
//   this.filesScanService
//     .addRemoteScanData(this.remotePiiScanRequestObj)
//     .pipe(
//       takeUntil(this.destroy$),
//       finalize(() => {
//         this.isLoading = false;
//         this.spinner.stop();
//       })
//     )
//     .subscribe({
//       next: (data) => {
//         console.log(data);
//         this.globalService.showAlert('success', 'Successfully Submitted!');
//         this.router.navigate(['/jrm/scan-files-details/files_list']);
//       },
//       error: (e) => {
//         console.error(e);
//         let msg: string;
//         if (e.error.errorCode == 409) {
//           msg = e.error.errorMessage;
//           this.globalService.showAlert('error', msg);
//         }else if(e.error.errorCode == 'INVALID_PATH'){
//           msg = e.error.errorMessage;
//           this.globalService.showAlert('error', msg);
//           this.router.navigate(['/jrm/scan-files-details/files_list']);
//         }
//          else if (e.status == 500) {
//           this.globalService.showAlert(
//             '500',
//             'Oops! There was an error while processing your request'
//           );
//         }
//       },
//       complete: () => console.info('complete'),
//     });
// }
