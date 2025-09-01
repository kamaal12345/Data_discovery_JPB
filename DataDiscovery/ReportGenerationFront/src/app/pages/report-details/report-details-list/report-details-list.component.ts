import {
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { SharedModule } from '../../../../shared/shared.module';
import { ActivatedRoute, Router } from '@angular/router';
import {
  NgbDate,
  NgbDateStruct,
  NgbCalendar,
  NgbDateParserFormatter,
} from '@ng-bootstrap/ng-bootstrap';
import { Subject, takeUntil, finalize } from 'rxjs';
import { PaginationRequest } from '../../../core/models/PaginationRequest';
import { PaginationResponseData } from '../../../core/models/PaginationResponseData';
import { GlobalService } from '../../../core/services/global-service.service';
import { VulnerabilitiesSummaryService } from '../../../core/services/vulnerabilities-summary.service';
import { ReportDetailsService } from '../../../core/services/report-details.service';
import { ReportDetails } from '../../../core/models/ReportDetails';
import { VulnerabilitiesSummary } from '../../../core/models/VulnerabilitiesSummary';
import { PdfGenerationComponent } from '../../templates/pdf-generation/pdf-generation.component';
import { PdfTriggerService } from '../../../core/services/pdf-trigger.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  selector: 'app-report-details-list',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './report-details-list.component.html',
  styleUrl: './report-details-list.component.css',
})
export class ReportDetailsListComponent implements OnInit, OnDestroy {
  @ViewChild(PdfGenerationComponent) pdfComponent!: PdfGenerationComponent;
  closeResult: string;
  isLoading: boolean;
  public destroy$: Subject<boolean>;
  paginationRequest: PaginationRequest;
  paginationResponseData: PaginationResponseData;
  page: number;
  pageLimits: number[];
  order: string;
  currentSort: string;
  totalResult: number;
  searchText: string;
  showSort = true;
  reportDetailsWithVulList: ReportDetails[];
  showError: boolean = false;
  vulnerabilityObj: VulnerabilitiesSummary;
  hoveredDate: NgbDate | null = null;
  fromDate: NgbDate | null;
  toDate: NgbDate | null = null;
  maxDate: NgbDateStruct;
  reportId: any;
  public Is_EMPLOYEE: boolean;

  constructor(
    public globalService: GlobalService,
    public vulnerabilitiesSummaryService: VulnerabilitiesSummaryService,
    public reportDetailsService: ReportDetailsService,
    private spinner: NgxUiLoaderService,
    private router: Router,
    private calendar: NgbCalendar,
    public formatter: NgbDateParserFormatter,
    private cdr: ChangeDetectorRef,
    private pdfTriggerService: PdfTriggerService,
    private route: ActivatedRoute
  ) {
    this.closeResult = '';
    this.isLoading = false;
    this.destroy$ = new Subject<boolean>();
    this.Is_EMPLOYEE = this.globalService.Is_EMPLOYEE;
    this.reportDetailsWithVulList = [];
    this.paginationRequest = {} as PaginationRequest;
    this.paginationResponseData = {} as PaginationResponseData;
    this.vulnerabilityObj = {} as VulnerabilitiesSummary;
    this.pageLimits = this.globalService.getPageLimits();
  }

  ngOnInit(): void {
    this.getReportDetailsWithVulListWP();
    this.reset();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  boxVisibility: boolean = true;
  toggleBox() {
    this.boxVisibility = !this.boxVisibility;
  }

  onPdfdownloadClick(reportId: number) {
    this.pdfTriggerService.triggerPdfGeneration(reportId, 'download');
  }

  onGeneratePdfClick(reportId: number) {
    this.pdfTriggerService.triggerPdfGeneration(reportId, 'preview');
  }

  reset() {
    this.order = 'DESC';
    this.currentSort = 'reportId';
    this.paginationRequest = {} as PaginationRequest;
    this.page = 1;
    this.totalResult = 10;
    this.paginationRequest.offset = 0;
    this.paginationRequest.pageSize = 10;
    this.paginationRequest.sort = 0;
    this.paginationRequest.field = 'reportId';
    this.paginationRequest.searchText = '';
    this.searchText = '';
    let date = new Date();
    this.getReportDetailsWithVulListWP();
  }

  onPageChange(event: any) {
    if (Number(event)) {
      this.paginationRequest.offset = +event - 1;
      this.getReportDetailsWithVulListWP();
    }
  }

  onPageLimitChange(event: any) {
    let value: string;
    value = (event.target as HTMLInputElement).value;
    this.paginationRequest.pageSize = +value;
    this.resetPageNumber();
    this.getReportDetailsWithVulListWP();
  }

  resetPageNumber() {
    this.page = 1;
    this.paginationRequest.offset = 0;
  }

  searchReportDetails() {
    this.resetPageNumber();
    this.destroy$.next(true);
    this.getReportDetailsWithVulListWP();
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
      this.getReportDetailsWithVulListWP();
    } else {
      this.currentSort = 'reportId';
    }
  }

  getIcon(name: string) {
    if (name === this.currentSort) {
      return this.order === 'ASC' ? 'fa-sort-up' : 'fa-sort-down';
    }
    return 'fa-sort';
  }

  getReportDetailsWithVulListWP(): void {
    this.isLoading = true;
    this.spinner.start();
    this.reportDetailsService
      .getAllReportDetailsList(this.paginationRequest)
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
          this.totalResult =
            this.paginationResponseData?.response?.totalElements;
          this.reportDetailsWithVulList = data?.response?.content;
          this.cdr.detectChanges();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }
}
