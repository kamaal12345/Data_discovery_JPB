import {
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { SharedModule } from '../../../../shared/shared.module';
import {
  FormGroup,
  FormArray,
  FormBuilder,
  Validators,
  ValidatorFn,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import { NgbModalRef, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Subject, takeUntil, finalize } from 'rxjs';
import editorConfig from '../../../../shared/editor-config';
import { ReportDetails } from '../../../core/models/ReportDetails';
import { VulnerabilitiesSummary } from '../../../core/models/VulnerabilitiesSummary';
import { GlobalService } from '../../../core/services/global-service.service';
import { ReportDetailsService } from '../../../core/services/report-details.service';
import { VulnerabilitiesSummaryService } from '../../../core/services/vulnerabilities-summary.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  selector: 'app-report-details-add',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './report-details-add.component.html',
  styleUrl: './report-details-add.component.css',
})
export class ReportDetailsAddComponent implements OnInit, OnDestroy {
  @ViewChild('content') content: any;
  modalRef: NgbModalRef;
  showError: boolean;
  isLoading: boolean;
  errorMsg: string;
  public destroy$: Subject<boolean>;
  isEdit: boolean;
  idToEdit: number;
  vulReportForm: FormGroup;
  vulnerabilities!: FormArray;
  reportDetailsObj: ReportDetails;
  fileExtentionType: string;
  fileBase64: string;
  config: AngularEditorConfig;
  headerImageBase64: string;
  headerImageExtentionType: string;
  duplicateReportDetails: any;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private fb: FormBuilder,
    public globalService: GlobalService,
    private spinner: NgxUiLoaderService,
    private modalService: NgbModal,
    private reportDetailsService: ReportDetailsService,
  ) {

    this.isLoading = false;
    this.showError = false;
    this.errorMsg = '';
    this.destroy$ = new Subject<boolean>();
    this.isEdit = false;
    this.idToEdit = 0;
    this.vulReportForm = new FormGroup({});
    this.reportDetailsObj = {} as ReportDetails;
    this.config = editorConfig;
    if (this.route.snapshot.params && this.route.snapshot.params['reportId']) {
      this.isEdit = true;
      this.idToEdit = +this.route.snapshot.params['reportId'];
      this.getReportDetailsData();
    }
  }

  ngOnInit(): void {
    this.buildForm();
    this.vulReportForm.controls['scope'].valueChanges.subscribe(() => {
      this.checkForDuplicates();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  resetForm() {
    if (this.isEdit) {
      this.vulReportForm.reset();
      this.getReportDetailsData();
    } else {
      this.buildForm();
    }
  }

  getEditorConfig(showImage: boolean): AngularEditorConfig {
    return {
      ...this.config,
      toolbarHiddenButtons: showImage
        ? [['bold'], ['insertVideo']]
        : [['bold'], ['insertImage', 'insertVideo']],
    };
  }

  getReportDetailsData() {
    this.isLoading = true;
    this.spinner.start();
    this.reportDetailsService
      .getReport(this.idToEdit)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false,
            this.spinner.stop();
        })
      )
      .subscribe({
        next: (data) => {
          console.log(data);
          this.reportDetailsObj = data;
          this.buildForm();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  cancel() {
    this.router.navigate(['jrm/report-details/list']);
  }

  private createValidators(): ValidatorFn[] {
    return [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(128),
    ];
  }

  buildForm(): void {
    this.vulReportForm = this.fb.group({
      typeOfTesting: [
        this.reportDetailsObj.typeOfTesting,
        this.createValidators(),
      ],
      scope: [this.reportDetailsObj.scope, this.createValidators()],
      headerText: [this.reportDetailsObj.headerText, this.createValidators()],
      footerText: [this.reportDetailsObj.footerText, this.createValidators()],
      applicationName: [
        this.reportDetailsObj.applicationName,
        this.createValidators(),
      ],
      assessmentType: [
        this.reportDetailsObj.assessmentType == 0 ? 0 : 1,
        [Validators.required],
      ],
      companyName: [this.reportDetailsObj.companyName, this.createValidators()],
      vulnerabilities: this.fb.array([]),
    });
    if (this.isEdit && this.reportDetailsObj?.vulnerabilities?.length > 0) {
      this.appendChildValues();
    } else {
      this.addVulnerabilityForm();
    }
  }

  createVulnerabilitiesForm(
    isEdit: boolean,
    vulnerability?: VulnerabilitiesSummary
  ): FormGroup {
    return this.fb.group({
      id: [vulnerability?.id ?? '', []],
      vulId: [vulnerability?.vulId ?? '', []],
      severity: [vulnerability?.severity ?? '', []],
      reportId: [vulnerability?.reportId ?? '', []],
      testDetails: [
        vulnerability?.testDetails ?? '',
        [Validators.required, Validators.minLength(2)],
      ],
      vulnerability: [
        vulnerability?.vulnerability ?? '',
        [Validators.required, Validators.minLength(2)],
      ],
      affectedScope: [
        vulnerability?.affectedScope ?? '',
        [Validators.required, Validators.minLength(2)],
      ],
      observation: [
        vulnerability?.observation ?? '',
        [Validators.required, Validators.minLength(2)],
      ],
      description: [
        vulnerability?.description ?? '',
        [Validators.required, Validators.minLength(2)],
      ],
      remediation: [
        vulnerability?.remediation ?? '',
        [Validators.required, Validators.minLength(2)],
      ],
      references: [
        vulnerability?.references ?? '',
        [Validators.required, Validators.minLength(2)],
      ],
    });
  }

  addVulnerabilityForm(): void {
    this.vulnerabilities = this.vulReportForm.get(
      'vulnerabilities'
    ) as FormArray;
    this.vulnerabilities.push(this.createVulnerabilitiesForm(false));
  }

  appendChildValues() {
    this.reportDetailsObj.vulnerabilities.forEach((element) => {
      this.vulnerabilities = this.vulReportForm.get(
        'vulnerabilities'
      ) as FormArray;
      this.vulnerabilities.push(this.createVulnerabilitiesForm(true, element));
    });
  }

  checkForDuplicates(): void {
    this.isLoading = true,
    this.spinner.start();
    const scope = this.vulReportForm.controls['scope'].value;
    if (scope) {
      this.reportDetailsService
        .checkScope(scope)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isLoading = false,
              this.spinner.stop();
          })
        )
        .subscribe({
          next: (scopeResponse) => {
            if (scopeResponse.isDuplicate) {
              const reportId = scopeResponse.reportId;
              this.reportDetailsService
                .getReport(reportId)
                .pipe(
                  takeUntil(this.destroy$),
                  finalize(() => {
                    this.isLoading = false,
                      this.spinner.stop();
                  })
                )
                .subscribe({
                  next: (reportDetails) => {
                    this.duplicateReportDetails = reportDetails;
                    this.modalService.open(this.content);
                  },
                  error: (e) => {
                    this.showError = true;
                    let msg = e?.error?.message
                      ? e?.error?.message
                      : 'Error fetching existing report details!';
                    msg = this.globalService.beautifyErrorMsg(msg);
                    this.errorMsg = msg;
                    this.globalService.showAlert('Error', msg);
                  },
                });
            } else {
              console.log('Report does not exit');
            }
          },
          error: (e) =>
            console.error('Error occurred while checking for duplicates:', e),
          complete: () => console.info('complete'),
        });
    }
  }

  closeModal() {
    this.modalService.dismissAll();
  }

  submit(): void {
    this.showError = false;
    if (!this.vulReportForm.valid) {
      this.globalService.showAlert(
        'Error',
        'Please enter all mandatory fields !'
      );
      return;
    }

    this.reportDetailsObj.typeOfTesting = this.vulReportForm.controls['typeOfTesting'].value;
    this.reportDetailsObj.scope = this.vulReportForm.controls['scope'].value;
    this.reportDetailsObj.headerText = this.vulReportForm.controls['headerText'].value;
    this.reportDetailsObj.footerText = this.vulReportForm.controls['footerText'].value;
    this.reportDetailsObj.assessmentType = this.vulReportForm.controls['assessmentType'].value == 1 ? 1 : 0;
    this.reportDetailsObj.applicationName = this.vulReportForm.controls['applicationName'].value;
    this.reportDetailsObj.companyName = this.vulReportForm.controls['companyName'].value;

    if (this.fileExtentionType && this.fileBase64) {
      this.reportDetailsObj.companyLogo = `data:image/${this.fileExtentionType};base64,${this.fileBase64}`;
    }

    if (this.headerImageExtentionType && this.headerImageBase64) {
      this.reportDetailsObj.headerImage = `data:image/${this.headerImageExtentionType};base64,${this.headerImageBase64}`;
    }
    this.isLoading = true,
    this.spinner.start();

    if (this.isEdit) {
      this.vulnerabilities.controls.forEach((element: any, index: number) => {
        let values = {} as VulnerabilitiesSummary;
        values.id = element.controls['id'].value;
        values.reportId = this.idToEdit;
        values.severity = element.controls['severity'].value;
        values.vulnerability = element.controls['vulnerability'].value;
        values.affectedScope = element.controls['affectedScope'].value;
        values.description = element.controls['description'].value;
        values.observation = element.controls['observation'].value;
        values.testDetails = element.controls['testDetails'].value;
        values.remediation = element.controls['remediation'].value;
        values.references = element.controls['references'].value;

        let i = this.reportDetailsObj.vulnerabilities.findIndex(
          (rec) => rec.id == element.controls['id'].value
        );
        if (i > -1) {
          this.reportDetailsObj.vulnerabilities[i].id = values.id;
          this.reportDetailsObj.vulnerabilities[i].reportId = values.reportId;
          this.reportDetailsObj.vulnerabilities[i].severity = values.severity;
          this.reportDetailsObj.vulnerabilities[i].vulnerability = values.vulnerability;
          this.reportDetailsObj.vulnerabilities[i].affectedScope = values.affectedScope;
          this.reportDetailsObj.vulnerabilities[i].observation = values.observation;
          this.reportDetailsObj.vulnerabilities[i].description = values.description;
          this.reportDetailsObj.vulnerabilities[i].testDetails = values.testDetails;
          this.reportDetailsObj.vulnerabilities[i].remediation = values.remediation;
          this.reportDetailsObj.vulnerabilities[i].references = values.references;
        } else {
          this.reportDetailsObj.vulnerabilities.push(values);
        }
      });

      this.reportDetailsService
        .editReportDetailsWithVulnerabilitiesFUI(this.reportDetailsObj)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isLoading = false,
              this.spinner.stop();
          })
        )
        .subscribe({
          next: (data) => {
            console.log(data);
            if (data) {
              this.globalService.showAlert(
                'Success',
                'Report Details updated successfully!'
              );
              this.router.navigate(['/jrm/report-details/list']);
            } else {
              this.showError = true;
              this.errorMsg = 'Unable to update Report Details!';
              this.globalService.showAlert(
                'Error',
                'Unable to update Report Details!'
              );
            }
          },
          error: (e) => {
            console.error(e);
            this.showError = true;
            let msg = e?.error?.message
              ? e?.error?.message
              : 'Error in updating Report Details!';
            msg = this.globalService.beautifyErrorMsg(msg);
            this.errorMsg = msg;
            this.globalService.showAlert('Error', msg);
          },
          complete: () => console.info('complete'),
        });
    } else {
      this.reportDetailsObj.createdDate = new Date().getTime();
      this.reportDetailsObj.vulnerabilities = [];
      this.vulnerabilities.controls.forEach((element: any) => {
        let values = {} as VulnerabilitiesSummary;
        values.id = element.controls['id'].value;
        values.reportId = element.controls['reportId'].value;
        values.severity = element.controls['severity'].value;
        values.vulnerability = element.controls['vulnerability'].value;
        values.affectedScope = element.controls['affectedScope'].value;
        values.description = element.controls['description'].value;
        values.testDetails = element.controls['testDetails'].value;
        values.observation = element.controls['observation'].value;
        values.remediation = element.controls['remediation'].value;
        values.references = element.controls['references'].value;
        this.reportDetailsObj.vulnerabilities.push(values);
      });
      this.reportDetailsService
        .addReportDetailsVulnerabilitiesFUI(this.reportDetailsObj)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isLoading = false,
            this.spinner.stop();
          })
        )
        .subscribe({
          next: (data) => {
            console.log(data);
            this.globalService.showAlert('success', 'Successfully Submitted!');
            this.router.navigate(['/jrm/report-details/list']);
          },
          error: (e) => {
            console.error(e);
            let msg: string;
            if (e.error.errorCode == 409) {
              msg = e.error.errorMessage;
              this.globalService.showAlert('error', msg);
            } else if (e.status == 500) {
              this.globalService.showAlert(
                '500',
                'Oops! There was an error while processing your request'
              );
            }
          },
          complete: () => console.info('complete'),
        });
    }
  }

  getErrorMessage(fieldName: string, fieldLabel: string): string {
    let msg = '';
    msg = this.globalService.getErrorMessage(
      this.vulReportForm,
      fieldName,
      fieldLabel
    );
    return msg;
  }

  getErrorMessageFormArray(index: number, fieldName: string, fieldLabel: string): string {
    let msg = '';
    let form: any = this.vulnerabilities.controls[index];
    if (form.controls[fieldName].touched) {
      if (form.controls[fieldName].hasError('required')) {
        msg = fieldLabel + ' required';
      } else if (form.controls[fieldName].hasError('minlength')) {
        let errors: any = form.controls[fieldName]?.errors;
        msg =
          'Min length (' + errors['minlength']?.requiredLength + ') required';
      } else if (form.controls[fieldName].hasError('maxlength')) {
        let errors: any = form.controls[fieldName]?.errors;
        msg =
          'Max length (' + errors['maxlength']?.requiredLength + ') exceeded';
      } else if (form.controls[fieldName].hasError('pattern')) {
        msg = 'Please enter valid Pattern';
      }
    }
    return msg;
  }

  async handleFileSelect(evt: any, type: 'logo' | 'header'): Promise<void> {
    const file: File = evt.target.files?.[0];
    if (!file) return;

    if (!this.validateFile(file)) return;

    const base64Full = await this.readFileAsBase64(file);
    const base64Data = base64Full.split(',')[1];
    const mimeType = file.type;
    const fileExt = mimeType.split('/')[1];

    if (type === 'logo') {
      this.fileBase64 = base64Data;
      this.fileExtentionType = fileExt;
    } else if (type === 'header') {
      this.headerImageBase64 = base64Data;
      this.headerImageExtentionType = fileExt;
    }
  }

  private readFileAsBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  validateFile(file: File): boolean {
    const allowedExtension = ['jpeg', 'jpg', 'png'];
    const fileExtension = file.name.split('.').pop()?.toLowerCase();
    const fileSizeMB = file.size / 1024 / 1024;

    // Validate file extension
    if (!allowedExtension.includes(fileExtension || '')) {
      alert('Allowed Extensions are: *.' + allowedExtension.join(', *.'));
      return false;
    }

    // Validate file size
    if (fileSizeMB > 5) {
      this.globalService.showAlert('Error', 'File size exceeds 5 MB');
      return false;
    }

    if (file.type !== `image/${fileExtension}` && !(fileExtension === 'jpg' && file.type === 'image/jpeg')) {
      alert('Mismatch between file extension and content type');
      return false;
    }

    return true;
  }
}
