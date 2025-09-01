import {
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Subject, takeUntil, finalize } from 'rxjs';
import { SharedModule } from '../../../../../shared/shared.module';
import { HostCategory } from '../../../../core/enums/HostCategory.enum';
import { GlobalService } from '../../../../core/services/global-service.service';
import { HostsService } from '../../../../core/services/hosts.service';
import { Host } from '../../../../core/models/Host';
import { HostCategoryNameEnum } from '../../../../core/enums/HostCategoryName.enum';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import editorConfig from '../../../../../shared/editor-config';

@Component({
  selector: 'app-hosts-add',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './hosts-add.component.html',
  styleUrl: './hosts-add.component.css',
})
export class HostsAddComponent implements OnInit, OnDestroy {
  @Input() modalRef!: NgbModalRef;
  @Input() id!: number | null;
  categories: { name: string; value: number }[] = [];
  closeResult: string;
  isLoading: boolean;
  public destroy$: Subject<boolean>;
  showError: boolean = false;
  hostObj: Host;
  hostForm: FormGroup;
  HostList: Host[] = [];
  isEdit: boolean;
  idToEdit: number;
  errorMsg: string;
  config: AngularEditorConfig;
  constructor(
    public globalService: GlobalService,
    public hostsService: HostsService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {
    this.closeResult = '';
    this.isLoading = false;
    this.errorMsg = '';
    this.destroy$ = new Subject<boolean>();
    this.isEdit = false;
    this.idToEdit = 0;
    this.hostForm = new FormGroup({});
    this.hostObj = {} as Host;
    this.config = editorConfig;
  }

  ngOnInit(): void {
    this.combineHostCategories();
    if (this.id) {
      this.isEdit = true;
      this.idToEdit = +this.id;
      this.getHostData();
    }
    this.buildForm();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  getEditorConfig(showImage: boolean): AngularEditorConfig {
    return {
      ...this.config,
      toolbarHiddenButtons: showImage
        ? [['bold'], ['insertVideo']]
        : [['bold'], ['insertImage', 'insertVideo']],
    };
  }


  buildForm(): void {
    this.hostForm = this.fb.group({
      name: [this.hostObj?.name ?? '', [Validators.required]],
      ip: [
        this.hostObj?.ip ?? '',
        [Validators.required, Validators.pattern(/^(\d{1,3}\.){3}\d{1,3}$/)],
      ],
      status: [this.isEdit? (this.hostObj.status == 1 ? 1 : 0) : 1 , [
        Validators.required,
      ]],
      category: [this.hostObj?.category, [Validators.required]],
      description: [this.hostObj?.description ?? ''],
    });
  }

    combineHostCategories(): void {
    this.categories = Object.keys(HostCategory)
      .filter(key => isNaN(Number(key))) // Filter out numeric keys
      .map(key => ({
        name: HostCategoryNameEnum[key as keyof typeof HostCategoryNameEnum],
        value: HostCategory[key as keyof typeof HostCategory]
      }));
      
    console.log(this.categories);  // Optionally log to see the result
  }


  cancel() {
    if (this.modalRef) {
      this.modalRef.dismiss('Cancel Clicked');
    }
  }

  getErrorMessage(fieldName: string, fieldLabel: string): string {
    let msg = '';
    msg = this.globalService.getErrorMessage(
      this.hostForm,
      fieldName,
      fieldLabel
    );
    return msg;
  }

  getHostData(): void {
    this.isLoading = true;
    this.hostsService
      .getHostData(this.idToEdit)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: (data) => {
          console.log(data);
          this.hostObj = data;
          this.buildForm();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  submit(): void {
    this.showError = false;
    this.errorMsg = '';
    if (!this.hostForm.valid) {
      this.globalService.showAlert(
        'Error',
        'Please enter all mandatory fields!'
      );
      return;
    }
    this.isLoading = true;
    this.hostObj.name = this.hostForm.controls['name'].value;
    this.hostObj.ip = this.hostForm.controls['ip'].value;
    this.hostObj.status = this.hostForm.controls['status'].value == 1 ? 1 : 0;
    this.hostObj.category = this.hostForm.controls['category'].value;
    this.hostObj.description = this.hostForm.controls['description'].value;
    this.hostObj.createdId = this.globalService.getUserId();
    if (this.isEdit) {
      this.hostObj.id = this.idToEdit;
      this.hostObj.UpdatedId = this.globalService.getUserId();
      this.hostObj.updatedDate = new Date().getTime();
      this.hostsService
        .editHosts(this.hostObj)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isLoading = false;
          })
        )
        .subscribe({
          next: (data) => {
            console.log(data);
            if (data) {
              this.globalService.showAlert(
                'Success',
                'Host Address updated successfully!'
              );
              if (this.modalRef) {
                this.modalRef.close('Form Submitted');
              }
            } else {
              this.showError = true;
              this.errorMsg = 'Unable to update Host Address!';
              this.globalService.showAlert(
                'Error',
                'Unable to update Host Address!'
              );
            }
          },
          error: (e) => {
            console.error(e);
            this.showError = true;
            let msg = e?.error?.message
              ? e?.error?.message
              : 'Error in updating Host Address!';
            msg = this.globalService.beautifyErrorMsg(msg);
            this.errorMsg = msg;
            this.globalService.showAlert('Error', msg);
          },
          complete: () => console.info('complete'),
        });
    } else {
      this.hostObj.createdDate = new Date().getTime();
      this.hostsService
        .addHosts(this.hostObj)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isLoading = false;
          })
        )
        .subscribe({
          next: (data) => {
            console.log(data);
            this.globalService.showAlert('success', 'Successfully Submitted!');
            if (this.modalRef) {
              this.modalRef.close('Form Submitted');
            }
          },
          error: (e) => {
            console.error(e);
            let msg: string;
            if (e.status == 409) {
              msg = e.error?.message || 'Duplicate IP Address or Duplicate Host Name.';
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
}
