import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../../../shared/shared.module';
import { finalize, Subject, takeUntil } from 'rxjs';
import { Users } from '../../../core/models/Users';
import { RoleNamesEnum, RolesEnum } from '../../../core/enums/Roles.enum';
import { PaginationRequest } from '../../../core/models/PaginationRequest';
import { PaginationResponseData } from '../../../core/models/PaginationResponseData';
import {
  NgbCalendar,
  NgbDate,
  NgbDateParserFormatter,
} from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';
import { GlobalService } from '../../../core/services/global-service.service';
import { UsersService } from '../../../core/services/users.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css',
})
export class UserListComponent implements OnInit, OnDestroy {
  isLoading: boolean;
  public destroy$: Subject<boolean>;
  usersList: Users[];
  rolesEnum = RolesEnum;
  roleNames = RoleNamesEnum;

  page: number;
  totalResult: number;
  paginationRequest: PaginationRequest;
  paginationResponseData: PaginationResponseData;
  pageLimits: number[];
  order: string;
  currentSort: string;
  showSort = true;
  hoveredDate: NgbDate | null = null;
  status: number | string = '';

  constructor(
    private router: Router,
    private cdr: ChangeDetectorRef,
    private calendar: NgbCalendar,
    public formatter: NgbDateParserFormatter,
    public globalService: GlobalService,
    private usersService: UsersService,
    private spinner: NgxUiLoaderService,
  ) {
    this.isLoading = false;
    this.destroy$ = new Subject<boolean>();
    this.usersList = [];
    this.paginationRequest = {} as PaginationRequest;
    this.paginationResponseData = {} as PaginationResponseData;
    this.pageLimits = this.globalService.getPageLimits();
  }

  ngOnInit(): void {
    this.reset();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  reset() {
    this.order = 'DESC';
    this.currentSort = 'userId';
    this.paginationRequest = {} as PaginationRequest;
    this.page = 1;
    this.totalResult = 10;
    this.paginationRequest.offset = 0;
    this.paginationRequest.pageSize = 10;
    this.paginationRequest.sort = 0;
    this.paginationRequest.field = 'userId';
    this.paginationRequest.searchText = '';
    this.getUsersList();
  }


  onPageChange(event: any) {
    if (Number(event)) {
      this.paginationRequest.offset = +event - 1;
      this.getUsersList();
    }
  }

  onPageLimitChange(event: any) {
    let value: string;
    value = (event.target as HTMLInputElement).value;
    this.paginationRequest.pageSize = +value;
    this.resetPageNumber();
    this.getUsersList();
  }

  resetPageNumber() {
    this.page = 1;
    this.paginationRequest.offset = 0;
  }

  searchUsers() {
    this.resetPageNumber();
    this.destroy$.next(true);
    this.getUsersList();
  }

  sortTable(name: string) {
    if (this.currentSort !== name) {
      this.order = 'ASC';
    }
    this.currentSort = name;
    if (this.currentSort) {
      this.order = this.order === 'ASC' ? 'DESC' : 'ASC';
      this.resetPageNumber();
      this.paginationRequest.sort = this.order === 'ASC' ? 1 : 0;
      this.paginationRequest.field = this.currentSort;
      this.getUsersList();
    } else {
      this.currentSort = 'id';
    }
  }

  getIcon(name: string) {
    if (name === this.currentSort) {
      return this.order === 'ASC' ? 'fa-sort-up' : 'fa-sort-down';
    }
    return 'fa-sort';
  }


  validateInput(currentValue: NgbDate | null, input: string): NgbDate | null {
    const parsed = this.formatter.parse(input);
    return parsed && this.calendar.isValid(NgbDate.from(parsed))
      ? NgbDate.from(parsed)
      : currentValue;
  }

  getTimeinMills(model: any) {
    let subDate = model;
    let date = new Date();
    date.setDate(subDate.day);
    date.setMonth(subDate.month - 1);
    date.setFullYear(subDate.year);
    return date.getTime();
  }

getUsersList(): void {
  this.isLoading = true;
  this.usersService
    .userList(this.paginationRequest)
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
        this.paginationResponseData = data;
        this.totalResult = this.paginationResponseData?.response?.totalElements;

        let users = data?.response?.content || [];
        if (this.globalService.IS_ADMIN) {
          users = users.filter(
            (user) => !user.roleValue.includes(this.rolesEnum.SUPER_ADMIN)
          );
          this.totalResult = users.length;
        }

        this.usersList = users;
        this.cdr.detectChanges();
      },
      error: (e) => console.error(e),
      complete: () => console.info('complete'),
    });
}


  useradd() {
    this.router.navigate(['/jrm/users/create']);
  }

  // callStatus(value: any) {
  //   this.paginationRequest.status = value;
  //   this.status = value;
  //   this.getUsersList();
  // }
}
