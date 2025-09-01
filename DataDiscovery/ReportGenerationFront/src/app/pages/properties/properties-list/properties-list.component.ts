import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, Subject, takeUntil } from 'rxjs';
import { SharedModule } from '../../../../shared/shared.module';
import { Property } from '../../../core/models/property';
import { PaginationRequest } from '../../../core/models/PaginationRequest';
import { PaginationResponseData } from '../../../core/models/PaginationResponseData';
import { GlobalService } from '../../../core/services/global-service.service';
import { PropertiesService } from '../../../core/services/properties.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  selector: 'app-properties-list',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './properties-list.component.html',
  styleUrl: './properties-list.component.css',
})
export class PropertiesListComponent implements OnInit, OnDestroy {
  isLoading: boolean;
  public destroy$: Subject<boolean>;
  propertyList: Property[];
  searchText: string;
  totalResult: number;
  page: number;
  paginationRequest: PaginationRequest;
  paginationResponseData: PaginationResponseData;
  pageLimits: number[];
  order: string;
  currentSort: string;
  showSort = true;
  showColumn = true;

  constructor(
    private router: Router,
    private cdr: ChangeDetectorRef,
    private globalService: GlobalService,
    private propertiesService: PropertiesService,
    private spinner: NgxUiLoaderService,
  ) {
    this.isLoading = false;
    this.destroy$ = new Subject<boolean>();
    this.propertyList = [];
    this.paginationRequest = {} as PaginationRequest;
    this.paginationResponseData = {} as PaginationResponseData;
    this.pageLimits = this.globalService.getPageLimits();
  }

  ngOnInit(): void {
    this.getPropertyList();
    this.reset();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  reset() {
    this.order = 'DESC';
    this.currentSort = 'id';
    this.paginationRequest = {} as PaginationRequest;
    this.page = 1;
    this.totalResult = 10;
    this.paginationRequest.offset = 0;
    this.paginationRequest.pageSize = 10;
    this.paginationRequest.sort = 0;
    this.paginationRequest.field = 'id';
    this.paginationRequest.searchText = '';
    let date = new Date();
    this.getPropertyList();
  }

  onPageChange(event: any) {
    if (Number(event)) {
      this.paginationRequest.offset = +event - 1;
      this.getPropertyList();
    }
  }

  onPageLimitChange(event: any) {
    let value: string;
    value = (event.target as HTMLInputElement).value;
    this.paginationRequest.pageSize = +value;
    this.resetPageNumber();
    this.getPropertyList();
  }

  resetPageNumber() {
    this.page = 1;
    this.paginationRequest.offset = 0;
  }

  searchCandidates() {
    this.resetPageNumber();
    this.destroy$.next(true);
    this.getPropertyList();
  }

  sortTable(name: string) {
    if (this.currentSort !== name) {
      this.order = 'ASC'; // resets the sort value
    }
    this.currentSort = name;
    if (this.currentSort) {
      this.order = this.order === 'ASC' ? 'DESC' : 'ASC';
      // set to first page
      this.resetPageNumber();
      this.paginationRequest.sort = this.order === 'ASC' ? 1 : 0;
      this.paginationRequest.field = this.currentSort;
      this.getPropertyList();
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

  getPropertyList(): void {
    this.isLoading = true;
    this.propertiesService
      .propertyList(this.paginationRequest)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          (this.isLoading = false), this.spinner.stop();
        })
      )
      .subscribe({
        next: (data) => {
          console.log(data);
          this.paginationResponseData = data;
          this.totalResult =
            this.paginationResponseData?.response?.totalElements;
          this.propertyList = data?.response?.content;
          this.cdr.detectChanges();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  propertiesadd() {
    this.router.navigate(['/jrm/properties/add-property']);
  }
}
