import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  NgbDateStruct,
  NgbModal,
  NgbModalRef,
} from '@ng-bootstrap/ng-bootstrap';
import { finalize, Subject, takeUntil } from 'rxjs';
import { Users } from '../../../core/models/Users';
import { RolesEnum } from '../../../core/enums/Roles.enum';
import { ActivatedRoute, Router } from '@angular/router';
import { GlobalService } from '../../../core/services/global-service.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DesignationEnum } from '../../../core/enums/Designation.enum';
import { UsersService } from '../../../core/services/users.service';
import { RolesService } from '../../../core/services/roles.service';
import { PropertyValue } from '../../../core/models/property';
import { PropertyValuesEnum } from '../../../core/enums/PropertyValuesEnum.enum';
import { PropertiesService } from '../../../core/services/properties.service';
import { SHA256 } from 'crypto-js';
import { SharedModule } from '../../../../shared/shared.module';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css',
})
export class UserFormComponent implements OnInit, OnDestroy {
  public destroy$: Subject<boolean>;
  maxDate: NgbDateStruct;
  minDate: NgbDateStruct;
  isLoading: boolean;
  showError: boolean;
  errorMsg: string;
  modalRef: NgbModalRef;
  isEdit: boolean;
  userForm: FormGroup;
  userObj: Users;
  idToEdit: number;
  department: number | 0;
  userId: number;
  rolesList: any[];
  designationEnum = DesignationEnum;
  propertyValuesEnum = PropertyValuesEnum;
  isRoleDisabled: boolean = false;
  filteredRolesList: any[];
  genderList: PropertyValue[];
  designationList: PropertyValue[];
  fileExtentionType: string;
  fileBase64: string;
  rolesEnum = RolesEnum;
  rolesSettings = {};
  designationSettings = {};
  active: number;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private fb: FormBuilder,
    public globalService: GlobalService,
    private usersService: UsersService,
    private rolesService: RolesService,
    private spinner: NgxUiLoaderService,
    private modalService: NgbModal,
    private propertiesService: PropertiesService
  ) {
    this.userId = this.globalService.getUserId();
    this.isLoading = false;
    this.showError = false;
    this.errorMsg = '';
    this.destroy$ = new Subject<boolean>();
    this.isEdit = false;
    this.userForm = new FormGroup({});
    this.userObj = {} as Users;
    this.idToEdit = 0;
    this.rolesList = [];
    let date = new Date();
    this.maxDate = {
      day: date.getDate(),
      month: date.getMonth() + 1,
      year: date.getFullYear(),
    };
    this.minDate = {
      day: date.getDate(),
      month: date.getMonth() + 1,
      year: date.getFullYear(),
    };
    if (this.route.snapshot.params && this.route.snapshot.params['userId']) {
      this.isEdit = true;
      this.idToEdit = +this.route.snapshot.params['userId'];
      this.getRolesList();
      this.getPropertiesData([this.propertyValuesEnum.DESIGNATION]);
      this.getUserData();
    }
  }

  ngOnInit(): void {
    this.rolesSettings = {
      singleSelection: false,
      primaryKey: 'roleValue',
      labelKey: 'roleName',
      text: 'Select Roles',
      selectAllText: 'Select All',
      unSelectAllText: 'UnSelect All',
      disabled: this.isRoleDisabled,
      badgeShowLimit: 3,
      enableSearchFilter: true,
      noDataAvailablePlaceholderText: 'There is no item availabale to show',
    };

    this.designationSettings = {
      singleSelection: false,
      primaryKey: 'value',
      labelKey: 'description',
      text: 'Select Designations Type',
      selectAllText: 'Select All',
      unSelectAllText: 'UnSelect All',
      badgeShowLimit: 4,
      enableSearchFilter: true,
      noDataAvailablePlaceholderText: 'There is no item availabale to show',
    };

    this.getPropertiesData([
      this.propertyValuesEnum.DESIGNATION,
      this.propertyValuesEnum.GENDER,
    ]);

    this.getRolesList();
    this.buildForm();
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
              const activeValues = (item.propertyValues || []).filter(
                (val: any) => val.status === true || val.status === 1
              );
              (this as any)[propertyKey] = activeValues;

              console.log(
                `Assigned ${propertyKey} (Active Only):`,
                activeValues
              );
            }
          });
        },
        error: (e) => console.error('Error fetching property values:', e),
        complete: () => console.info('Property values fetch complete'),
      });
  }
  getRolesList(): void {
    this.isLoading = true;
    this.rolesService
      .rolesList()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: (data) => {
          console.log(data);
          this.rolesList = data;
          this.filteredRolesList = this.rolesList.filter(
            (role) => role.roleValue !== this.rolesEnum.SUPER_ADMIN
          );
          if (!this.isEdit) {
            this.buildForm();
          }
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  getUserData(): void {
    this.isLoading = true;
    this.usersService
      .viewUser(this.idToEdit)
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

  buildForm(): void {
    const roles =
      this.isEdit && this.userObj.roleValue
        ? this.getRoleNameFromValue(this.userObj.roleValue)
        : [];

    if (!this.isEdit) {
      if (!roles.some((role) => role.roleValue === this.rolesEnum.EMPLOYEE)) {
        const employeeRole = this.rolesList.find(
          (role) => role.roleValue === this.rolesEnum.EMPLOYEE
        );
        if (employeeRole) {
          roles.push(employeeRole);
        }
      }
    }

    let designationTypes =
      this.isEdit && this.userObj.designation
        ? this.getDesignationTypeFromValue(this.userObj.designation)
        : [];

    this.userForm = this.fb.group({
      username: [
        this.userObj.username,
        [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(128),
          Validators.pattern('^$|^([a-zA-Z]+([a-zA-Z0-9._]+)*)*$'),
        ],
      ],
      password: [this.userObj.password, this.passwordValidations()],
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
      middleName: [
        this.userObj.middleName,
        [
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
      gender: [this.userObj.gender, [Validators.required]],
      dob: [this.getDateObjFromMillis(this.userObj.dob), [Validators.required]],
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
      comments: [this.userObj.comments, []],
      role: [roles, [Validators.required]],
      status: [this.userObj.status == 0 ? 0 : 1, [Validators.required]],

      designation: [designationTypes || [], this.getValidators('designation')],
      // 'department': [this.userObj?.department || '', this.getValidators('department')],
    });

    if (this.isEdit) {
      if (
        this.userObj.roleValue[0] === this.rolesEnum.SUPER_ADMIN &&
        this.globalService.IS_SUPER_ADMIN
      ) {
        this.isRoleDisabled = true;
        this.rolesSettings = Object.assign({}, this.rolesSettings, {
          disabled: this.isRoleDisabled,
        });
      }
    }
  }

  setDateFromDateObj(fieldName: string, form: any) {
    let value: any;
    let datePickerDate = form.controls[fieldName].value;
    if (datePickerDate) {
      let date = new Date();
      date.setDate(datePickerDate.day);
      date.setMonth(datePickerDate.month - 1);
      date.setFullYear(datePickerDate.year);
      value = date.getTime();
    } else {
      value = null;
    }
    return value;
  }

  getDateObjFromMillis(value: number) {
    let model: any;
    if (value) {
      let datePickerDate = new Date(value);
      model = {
        day: datePickerDate.getDate(),
        month: datePickerDate.getMonth() + 1,
        year: datePickerDate.getFullYear(),
      };
    }
    return model;
  }

  cancel() {
    this.router.navigate(['/jrm/users']);
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

  isFieldRequired(field: string): boolean {
    if (this.globalService.IS_SUPER_ADMIN) {
      return false;
    } else {
      return field === 'designation';
      //  || field === 'department';
    }
  }

  getValidators(field: string): any {
    if (this.globalService.IS_SUPER_ADMIN) {
      if (
        field === 'designation'
        // field === 'department' ||
      ) {
        return [];
      }
    } else {
      return [Validators.required];
    }
  }

  passwordValidations() {
    let value: any[] = [
      Validators.minLength(6),
      Validators.maxLength(128),
      Validators.pattern(
        /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{6,}$/
      ),
    ];
    if (!this.isEdit) {
      value.push(Validators.required);
    }
    return value;
  }

  roleValidations() {
    let value: any[] = [];
    if (!this.isEdit) {
      value.push(Validators.required);
    }
    return value;
  }

  getRoleNameFromValue(values: number[]): any[] {
    if (!values || !this.rolesList) {
      return [];
    }
    return values.map((value) => {
      const role = this.rolesList.find((rec) => rec.roleValue === value);
      return role;
    });
  }

  getDesignationTypeFromValue(values: number[]): any[] {
    if (!values || !this.designationList) {
      return [];
    }
    return values
      .map((value) => {
        const designationType = this.designationList.find(
          (rec) => rec.value === value
        );
        console.log('Designation Type for value', value, ':', designationType);
        return designationType;
      })
      .filter((item) => item);
  }

  onPassowrdChange(event: any) {
    let value: string;
    value = (event.target as HTMLInputElement).value;
    this.userForm.controls['password'].setValue(value);
    this.userForm.controls['password'].markAsTouched();
  }

  handleFileSelect(evt: any) {
    if (!this.validateFile(evt)) {
      return;
    }
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
    this.userObj.username = this.userForm.controls['username'].value;
    this.userObj.employeeId = this.userForm.controls['employeeId'].value;
    this.userObj.firstName = this.userForm.controls['firstName'].value;
    this.userObj.middleName = this.userForm.controls['middleName'].value;
    this.userObj.lastName = this.userForm.controls['lastName'].value;
    this.userObj.gender = this.userForm.controls['gender'].value;
    this.userObj.dob = this.setDateFromDateObj('dob', this.userForm);
    this.userObj.mobilePhone = this.userForm.controls['mobile'].value;
    this.userObj.email = this.userForm.controls['email'].value;
    this.userObj.roleValue = this.userForm.controls['role'].value.map(
      (role: any) => role.roleValue
    );

    const status = (this.userObj.status =
      this.userForm.controls['status'].value == 1 ? 1 : 0);
    this.userObj.comments = this.userForm.controls['comments'].value;
    this.userObj.metaStatus = 1;
    this.userObj.loggedIn = 0;
    this.userObj.designation = this.userForm.controls['designation'].value.map(
      (designation: any) => designation.value
    );
    // this.userObj.department = this.userForm.controls['department'].value;
    let password: string = this.userForm.controls['password'].value;
    if (password && password.trim() !== '') {
      let hashedPassword = SHA256(password).toString();
      this.userObj.password = hashedPassword;
    } else {
      this.userObj.password = password;
    }

    if (this.fileExtentionType && this.fileBase64) {
      this.userObj.profileImg =
        'data:application/' +
        this.fileExtentionType +
        ';base64,' +
        this.fileBase64;
    }

    this.isLoading = true;
    this.spinner.start();
    if (this.isEdit) {
      this.userObj.userId = this.idToEdit;
      if (
        this.isEdit &&
        this.userObj.roleValue.toString() !==
          this.userForm.controls['role'].value.toString()
      ) {
        this.userObj.createdById = this.globalService.getUserId();
        this.userObj.createdDate = new Date().getTime();
      }
      this.userObj.updatedById = this.globalService.getUserId();
      this.userObj.updatedDate = new Date().getTime();
      this.usersService
        .editUser(this.userObj)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            (this.isLoading = false), this.spinner.stop();
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
              this.router.navigate(['/jrm/users']);
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
    } else {
      this.userObj.createdById = this.globalService.getUserId();
      this.usersService
        .addUser(this.userObj)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            (this.isLoading = false), this.spinner.stop();
          })
        )
        .subscribe({
          next: (data) => {
            console.log(data);
            if (data) {
              this.globalService.showAlert(
                'Success',
                'User created successfully !'
              );
              this.router.navigate(['/jrm/users']);
            } else {
              this.showError = true;
              this.errorMsg = 'Unable to create User !';
              this.globalService.showAlert('Error', 'Unable to create User !');
            }
          },
          error: (e) => {
            console.error(e);
            this.showError = true;
            let msg = e?.error?.message
              ? e?.error?.message
              : 'Error in creating User !';
            msg = this.globalService.beautifyErrorMsg(msg);
            this.errorMsg = msg;
            this.globalService.showAlert('Error', msg);
          },
          complete: () => console.info('complete'),
        });
    }
  }
}
