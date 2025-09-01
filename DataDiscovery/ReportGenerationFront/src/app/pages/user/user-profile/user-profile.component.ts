import { Component, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../../../shared/shared.module';
import { finalize, Subject, takeUntil } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Users } from '../../../core/models/Users';
import { PropertyValuesEnum } from '../../../core/enums/PropertyValuesEnum.enum';
import { Router } from '@angular/router';
import { GlobalService } from '../../../core/services/global-service.service';
import { PropertiesService } from '../../../core/services/properties.service';
import { UsersService } from '../../../core/services/users.service';
import { PropertyValue } from '../../../core/models/property';
import { SHA256 } from 'crypto-js';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css',
})
export class UserProfileComponent implements OnInit, OnDestroy {
  isLoading: boolean;
  showError: boolean;
  errorMsg: string;
  public destroy$: Subject<boolean>;
  userForm: FormGroup;
  userObj: Users;
  userId: number;
  userDetailsId: number;
  userRole: any;
  // maxDate: NgbDateStruct;
  // minDate: NgbDateStruct;
  // departmentList: any[];
  designationList: PropertyValue[];
  fileExtentionType: string;
  fileBase64: string;
  propertyValuesEnum = PropertyValuesEnum;
  referralType: string = '';

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private globalService: GlobalService,
    private propertiesService: PropertiesService,
    private usersService: UsersService
  ) {
    this.isLoading = false;
    this.showError = false;
    this.errorMsg = '';
    this.destroy$ = new Subject<boolean>();
    this.userForm = new FormGroup({});
    this.userObj = {} as Users;
    this.userId = this.globalService.getUserId();
    this.userRole = this.globalService.getUserRoleType();
  }

  ngOnInit(): void {
    this.buildForm();
    this.getUserData();

    this.getPropertiesData([
      this.propertyValuesEnum.DESIGNATION,
      this.propertyValuesEnum.GENDER,
    ]);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  getPropertiesData(ids: number[]) {
    if (!ids || ids.length === 0) {
      console.warn('No property IDs passed.');
      return;
    }

    const propertyListsMap: { [key: number]: string } = {
      [this.propertyValuesEnum.DESIGNATION]: 'designationList',
      [this.propertyValuesEnum.GENDER]: 'genderList',
    };

    this.isLoading = true;

    this.propertiesService
      .getPropertyValuesListFromMultipleIds(ids)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: (data) => {
          console.log('Fetched Data:', data);

          data.forEach((item: any, index: number) => {
            const propertyKey = propertyListsMap[ids[index]];
            if (propertyKey) {
              (this as any)[propertyKey] = item.propertyValues || [];
              console.log(`Assigned ${propertyKey}:`, item.propertyValues);
            }
          });
        },
        error: (e) => console.error('Error fetching property values:', e),
        complete: () => console.info('Property values fetch complete'),
      });
  }

  getUserData(): void {
    this.isLoading = true;
    this.usersService
      .viewUserProfile(this.userId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: (data) => {
          console.log(data);
          this.userObj = data;
          this.buildForm();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  getNameFromPropertyValue(list: PropertyValue[], value: number): any {
    let returnValue = null;
    if (list && list.length > 0) {
      let i = list.findIndex((rec) => rec.value == value);
      if (i > -1) {
        returnValue = list[i].description;
      }
    }
    return returnValue;
  }

  buildForm(): void {
    this.userForm = this.fb.group({
      username: [
        {
          value: this.userObj.username,
          disabled: true,
        },
        [],
      ],
      password: [
        this.userObj.password,
        [Validators.minLength(4), Validators.maxLength(128)],
      ],
      employeeId: [
        this.userObj.employeeId,
        [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(128),
          Validators.pattern('^$|^([a-zA-Z]+([a-zA-Z0-9]+)*)*$'),
        ],
      ],
      firstName: [
        this.userObj.firstName,
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(128),
          Validators.pattern('^$|^([a-zA-Z]+([a-zA-Z .()-]+)*)*$'),
        ],
      ],
      lastName: [
        this.userObj.lastName,
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(128),
          Validators.pattern('^$|^([a-zA-Z]+([a-zA-Z .()-]+)*)*$'),
        ],
      ],
      middleName: [
        this.userObj.middleName,
        [
          Validators.minLength(3),
          Validators.maxLength(128),
          Validators.pattern('^$|^([a-zA-Z]+([a-zA-Z .()-]+)*)*$'),
        ],
      ],
      mobile: [
        this.userObj.mobilePhone,
        [
          Validators.required,
          Validators.minLength(10),
          Validators.maxLength(10),
          Validators.pattern('^$|^[0-9]{10}$'),
        ],
      ],
      email: [
        this.userObj.email,
        [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(128),
          Validators.pattern(
            '^$|^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@' +
              '[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$'
          ),
        ],
      ],
      role: [
        {
           value: this.userRole,
          disabled: true,
        },
        [Validators.required],
      ],

      designation: [
        {
          value: this.userObj.designation,
          disabled: true,
        },
      ],
      // 'department': [{ value: this.userObj.department, disabled: true }],
    });
  }

  cancel() {
    this.router.navigate(['/jrm/dashboard']);
  }
  getErrorMessage(fieldName: string, fieldLabel: string): string {
    let msg = '';
    msg = this.globalService.getErrorMessage(
      this.userForm,
      fieldName,
      fieldLabel
    );
    return msg;
  }

  onPassowrdChange(event: any) {
    let value: string;
    value = (event.target as HTMLInputElement).value;
    this.userForm.controls['password'].setValue(value);
  }

  handleFileSelect(evt: any) {
    if (!this.validateFile(evt)) {
      return;
    }
    // const files: File = evt.target.files;
    const files: any = evt.target.files;
    const file: File = files[0];
    const pattern = /image-*/;
    if (!file.type.match(pattern)) {
      this.globalService.showAlert(
        'Error',
        'Supported formats .jpg or .jpeg or .png !'
      );
      return;
    }
    const FileSize = evt.target.files[0].size / 1024 / 1024; // in MB
    if (FileSize > 5) {
      this.globalService.showAlert('Error', 'File size exceeds 5 MB');
      return;
    }
    if (files && file) {
      const reader = new FileReader();
      // this.imageName = evt.target.files[0].name;
      reader.onload = this._handleReaderLoadedSign.bind(this);
      reader.readAsBinaryString(file);
    }
  }
  _handleReaderLoadedSign(readerEvt: any) {
    const binaryString = readerEvt.target.result;
    this.fileBase64 = btoa(binaryString);
  }
  validateFile(evt: any) {
    const allowedExtension = ['jpeg', 'jpg', 'png'];
    const fileExtension = evt.target.value.split('.').pop().toLowerCase();
    let isValidFile = false;

    for (let index in allowedExtension) {
      if (fileExtension === allowedExtension[index]) {
        this.fileExtentionType = fileExtension;
        isValidFile = true;
        break;
      }
    }

    if (!isValidFile) {
      alert('Allowed Extensions are : *.' + allowedExtension.join(', *.'));
    }
    const FileSize = evt.target.files[0].size / 1024 / 1024; // in MB
    if (FileSize > 5) {
      this.globalService.showAlert('Error', 'File size exceeds 5 MB');
      return null;
    }

    return isValidFile;
  }

  submit() {
    this.showError = false;
    if (!this.userForm.valid) {
      this.globalService.showAlert(
        'Error',
        'Please enter all mandatory fields !'
      );
      return;
    }
    this.userObj.userId = this.userId;
    this.userObj.firstName = this.userForm.controls['firstName'].value;
    this.userObj.lastName = this.userForm.controls['lastName'].value;
    this.userObj.middleName = this.userForm.controls['middleName'].value;
    this.userObj.mobilePhone = this.userForm.controls['mobile'].value;
    this.userObj.email = this.userForm.controls['email'].value;
    this.userObj.designation = this.userForm.controls['designation'].value;
    // this.userObj.department = this.userForm.controls['department'].value;

    let password: string = this.userForm.controls['password'].value;
    if (password && password.trim() !== '') {
      let hashedPassword = SHA256(password).toString();
      this.userObj.password = hashedPassword;
    } else {
      this.userObj.password = password;
    }
    this.userObj.employeeId = this.userObj.employeeId;
    this.userObj.username = this.userObj.username;
    this.userObj.roleValue = this.userObj.roleValue;
    this.userObj.updatedById = this.globalService.getUserId();
    this.userObj.createdById = this.globalService.getUserId();

    if (this.fileExtentionType && this.fileBase64) {
      this.userObj.profileImg =
        'data:application/' +
        this.fileExtentionType +
        ';base64,' +
        this.fileBase64;
    }

    this.isLoading = true;
    this.usersService
      .editUserProfile(this.userObj)
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
              'User updated successfully !'
            );
            this.router.navigate(['jrm/dashboard']);
          } else {
            this.showError = true;
            this.errorMsg = 'Unable to update User !';
            this.globalService.showAlert('Error', 'Unable to update User !');
          }
        },
        error: (e) => {
          console.error(e);
          this.showError = true;
          let msg = e?.error?.message
            ? e?.error?.message
            : 'Error in updating User !';
          msg = this.globalService.beautifyErrorMsg(msg);
          this.errorMsg = msg;
          this.globalService.showAlert('Error', msg);
        },
        complete: () => console.info('complete'),
      });
  }
}
