import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize, Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router, RouterOutlet } from '@angular/router';
import { Property, PropertyValue } from '../../../core/models/property';
import { GlobalService } from '../../../core/services/global-service.service';
import { PropertiesService } from '../../../core/services/properties.service';
import { SharedModule } from '../../../../shared/shared.module';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  selector: 'app-properties-form',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './properties-form.component.html',
  styleUrl: './properties-form.component.css',
})
export class PropertiesFormComponent implements OnInit {
  userId: number;
  isLoading: boolean;
  public destroy$: Subject<boolean>;
  isEdit: boolean;
  propertyForm: FormGroup;
  propertyObj: Property;
  idToEdit: number;
  propertyValues!: FormArray;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private globalService: GlobalService,
    private propertyService: PropertiesService,
    private spinner: NgxUiLoaderService,
  ) {
    this.userId = this.globalService.getUserId();
    this.isLoading = false;
    this.destroy$ = new Subject<boolean>();
    this.isEdit = false;
    this.propertyForm = new FormGroup({});
    this.propertyObj = {} as Property;
    this.idToEdit = 0;
    if (
      this.route.snapshot.params &&
      this.route.snapshot.params['propertyId']
    ) {
      this.isEdit = true;
      this.idToEdit = +this.route.snapshot.params['propertyId'];
      this.getPropertiesData();
    }
  }

  ngOnInit(): void {
    this.buildForm();
  }

  getPropertiesData(): void {
    this.isLoading = true;
    this.spinner.start();
    this.propertyService
      .viewProperty(this.idToEdit)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          (this.isLoading = false), this.spinner.stop();
        })
      )
      .subscribe({
        next: (data) => {
          console.log(data);
          this.propertyObj = data;
          this.buildForm();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  resetForm() {
    if (this.isEdit) {
      this.propertyForm.reset();
      this.getPropertiesData();
    } else {
      this.buildForm();
    }
  }

  buildForm(): void {
    this.propertyForm = this.fb.group({
      propertyName: [
        this.propertyObj.name,
        [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(128),
          Validators.pattern('^$|^([a-zA-Z]+([a-zA-Z .()-]+)*)*$'),
        ],
      ],
      propertyValue: [
        this.propertyObj.lookupCodeSetValue,
        [
          Validators.required,
          Validators.minLength(1),
          Validators.maxLength(2),
          Validators.pattern('^$|^([0-9]+([0-9]+)*)*$'),
        ],
      ],
      status: [
        this.isEdit ? (this.propertyObj.status ? 1 : 0) : 1,
        [Validators.required],
      ],
      propertyValues: this.fb.array([]),
    });
    if (this.isEdit && this.propertyObj?.propertyValues?.length > 0) {
      this.appendChildValues();
    } else {
      this.addPropertyValueItem();
    }
  }

  createPropertyValueItems(
    isEdit: boolean,
    id?: number,
    name?: string,
    value?: number,
    status?: number
  ): FormGroup {
    return this.fb.group({
      id: [id ? id : '', []],
      description: [
        name ? name : '',
        [
          Validators.required,
          Validators.minLength(1),
          Validators.maxLength(128),
          Validators.pattern('^$|^([0-9a-zA-Z]+([0-9a-zA-Z .()-]+)*)*$'),
        ],
      ],
      value: [
        value ? value : '',
        [
          Validators.required,
          Validators.minLength(1),
          Validators.maxLength(2),
          Validators.pattern('^$|^([0-9]+([0-9]+)*)*$'),
        ],
      ],
      status: [isEdit ? status : 1, [Validators.required]],
    });
  }

  addPropertyValueItem(): void {
    this.propertyValues = this.propertyForm.get('propertyValues') as FormArray;
    this.propertyValues.push(this.createPropertyValueItems(false));
  }

  appendChildValues() {
    this.propertyObj.propertyValues.forEach((element) => {
      this.propertyValues = this.propertyForm.get(
        'propertyValues'
      ) as FormArray;
      this.propertyValues.push(
        this.createPropertyValueItems(
          true,
          element.id,
          element.description,
          element.value,
          element.status ? 1 : 0
        )
      );
    });
  }

  cancel() {
    this.router.navigate(['/jrm/properties/properties-list']);
  }

  getErrorMessage(fieldName: string, fieldLabel: string): string {
    let msg = '';
    msg = this.globalService.getErrorMessage(
      this.propertyForm,
      fieldName,
      fieldLabel
    );
    return msg;
  }

  getErrorMessageFormArray(
    index: number,
    fieldName: string,
    fieldLabel: string
  ): string {
    let msg = '';
    let form: any = this.propertyValues.controls[index];
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

  submit() {
    console.log(this.propertyForm.controls);
    if (!this.propertyForm.valid) {
      this.globalService.showAlert(
        'Error',
        'Please enter all mandatory fields !'
      );
      return;
    }

    this.isLoading = true;
    this.spinner.start();
    this.propertyObj.name = this.propertyForm.controls['propertyName'].value;
    this.propertyObj.lookupCodeSetValue = Number(
      this.propertyForm.controls['propertyValue'].value
    );
    let status = this.propertyForm.controls['status'].value;
    this.propertyObj.status = status == 1 ? true : false;

    if (this.isEdit) {
      this.propertyObj.updatedById = this.userId;

      this.propertyValues.controls.forEach((element: any, index: number) => {
        let values = {} as PropertyValue;
        values.description = element.controls['description'].value;
        values.value = Number(element.controls['value'].value);
        values.status = element.controls['status'].value == 1 ? true : false;
        let i = this.propertyObj.propertyValues.findIndex(
          (rec) => rec.id == element.controls['id'].value
        );
        if (i > -1) {
          this.propertyObj.propertyValues[i].description = values.description;
          this.propertyObj.propertyValues[i].value = values.value;
          this.propertyObj.propertyValues[i].status = values.status;
        } else {
          this.propertyObj.propertyValues.push(values);
        }
      });

      this.propertyService
        .editProperty(this.propertyObj)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isLoading = false;
            this.spinner.stop();
          })
        )
        .subscribe({
          next: (data) => {
            console.log(data);
            if (data) {
              this.globalService.showAlert(
                'Success',
                'Property updated successfully !'
              );
              this.router.navigate(['/jrm/properties']);
            } else {
              this.globalService.showAlert(
                'Error',
                'Unable to update Property !'
              );
            }
          },
          error: (e) => {
            console.error(e);
            this.globalService.showAlert(
              'Error',
              'Error in updating Property !'
            );
          },
          complete: () => console.info('complete'),
        });
    } else {
      this.propertyObj.createdById = this.userId;
      this.propertyObj.propertyValues = [];
      this.propertyValues.controls.forEach((element: any) => {
        let values = {} as PropertyValue;
        values.description = element.controls['description'].value;
        values.value = Number(element.controls['value'].value);
        values.status = element.controls['status'].value == 1 ? true : false;
        this.propertyObj.propertyValues.push(values);
      });

      this.propertyService
        .addProperty(this.propertyObj)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isLoading = false;
            this.spinner.stop();
          })
        )
        .subscribe({
          next: (data) => {
            console.log(data);
            if (data) {
              this.globalService.showAlert(
                'Success',
                'Property created successfully !'
              );
              this.router.navigate(['/jrm/properties']);
            } else {
              this.globalService.showAlert(
                'Error',
                'Unable to create Property !'
              );
            }
          },
          error: (e) => {
            console.error(e);
            this.globalService.showAlert(
              'Error',
              'Error in creating Property !'
            );
          },
          complete: () => console.info('complete'),
        });
    }
  }
}
