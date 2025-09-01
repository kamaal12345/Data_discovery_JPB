import { ChangeDetectorRef, Component, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { NgbModal, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { Subject, takeUntil, finalize } from 'rxjs';
import { PaginationRequest } from '../../../core/models/PaginationRequest';
import { PaginationResponseData } from '../../../core/models/PaginationResponseData';
import { PiiScanRequest } from '../../../core/models/PiiScanRequest';
import { FilesScanService } from '../../../core/services/files-scan.service';
import { GlobalService } from '../../../core/services/global-service.service';
import { Router } from '@angular/router';
import { SharedModule } from '../../../../shared/shared.module';
import { ScanFilesDetailsResultsComponent } from '../scan-files-details-results/scan-files-details-results.component';

@Component({
  selector: 'app-scan-files-details-list',
  standalone: true,
  imports: [SharedModule, ScanFilesDetailsResultsComponent],
  templateUrl: './scan-files-details-list.component.html',
  styleUrl: './scan-files-details-list.component.css',
})
export class ScanFilesDetailsListComponent implements OnInit, OnDestroy {
  @ViewChildren(ScanFilesDetailsResultsComponent) resultComponents!: QueryList<ScanFilesDetailsResultsComponent>;
  isLoading: boolean;
  closeResult: string;
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
  scanDataList: PiiScanRequest[];
  accordionVisibility: boolean[] = [];
  currentIndex: number = 0;
  showError: boolean = false;
  requestDetails: PiiScanRequest;
  // public IS_SUPER_ADMIN: boolean;
  // public IS_ADMIN: boolean;
  public Is_EMPLOYEE: boolean;
  progressMap: {
    [requestId: number]: {
      scannedFiles: number;
      totalFiles: number;
      percentage: number;
    };
  } = {};
  active = 1;

  constructor(
    public globalService: GlobalService,
    public filesScanService: FilesScanService,
    private spinner: NgxUiLoaderService,
    private modalService: NgbModal,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.isLoading = false;
    this.closeResult = '';
    this.destroy$ = new Subject<boolean>();
    // this.IS_SUPER_ADMIN = this.globalService.IS_SUPER_ADMIN
    // this.IS_ADMIN = this.globalService.IS_ADMIN;
    this.Is_EMPLOYEE = this.globalService.Is_EMPLOYEE;
    this.scanDataList = [];
    this.paginationRequest = {} as PaginationRequest;
    this.paginationResponseData = {} as PaginationResponseData;
    this.pageLimits = this.globalService.getPageLimits();
  }

  ngOnInit(): void {
    this.getFileScanDataList();
    this.reset();
    if (this.scanDataList && this.scanDataList.length > 0) {
      this.accordionVisibility = this.scanDataList.map((_, i) => i === 0);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  toggleBox(index: number): void {
    if (this.accordionVisibility[index]) {
      const nextIndex = (index + 1) % this.accordionVisibility.length;
      this.accordionVisibility = this.accordionVisibility.map(
        (_, i) => i === nextIndex
      );
      this.currentIndex = nextIndex;
    } else {
      this.accordionVisibility = this.accordionVisibility.map(
        (_, i) => i === index
      );
      this.currentIndex = index;
    }
  }

  openAllAccordions(): void {
    this.accordionVisibility = this.scanDataList.map(() => true);
  }

  reset() {
    this.order = 'DESC';
    this.currentSort = 'filePath';
    this.paginationRequest = {} as PaginationRequest;
    this.page = 1;
    this.totalResult = 10;
    this.paginationRequest.offset = 0;
    this.paginationRequest.pageSize = 10;
    this.paginationRequest.sort = 1;
    this.paginationRequest.field = 'createdDate';
    this.paginationRequest.searchText = '';
    this.searchText = '';
    this.getFileScanDataList();
  }

  onPageChange(event: any) {
    if (Number(event)) {
      this.paginationRequest.offset = +event - 1;
      this.getFileScanDataList();
    }
  }

  onPageLimitChange(event: any) {
    let value: string;
    value = (event.target as HTMLInputElement).value;
    this.paginationRequest.pageSize = +value;
    this.resetPageNumber();
    this.getFileScanDataList();
  }

  resetPageNumber() {
    this.page = 1;
    this.paginationRequest.offset = 0;
  }

  searchMatchedData() {
    this.resetPageNumber();
    this.destroy$.next(true);
    // this.getFileScanDataList();
  }

  getFileScanDataList(): void {
    this.isLoading = true;
    this.spinner.start();
    this.filesScanService
      .getAllScanDataList(this.paginationRequest)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
          this.spinner.stop();
        })
      )
      .subscribe({
        next: (data) => {
          this.paginationResponseData = data;
          this.totalResult =
            this.paginationResponseData?.response?.totalElements;
          this.scanDataList = data?.response?.content;
          const isSearch = this.searchText?.trim().length > 0;
          if (isSearch) {
            this.accordionVisibility = this.scanDataList.map(() => true);
          } else {
            this.accordionVisibility = this.scanDataList.map((_, i) => i === 0);
          }

          this.cdr.detectChanges();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  open(content: any, requestId: any, size: any) {
    const scanData = this.scanDataList.find(
      (scan) => scan.requestId === requestId
    );
    if (scanData) {
      this.requestDetails = scanData;
      this.modalService
        .open(content, {
          ariaLabelledBy: 'modal-basic-title',
          size: size,
        })
        .result.then(
          (result) => {
            this.closeResult = `Closed with: ${result}`;
          },
          (reason) => {
            this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
          }
        );
    } else {
      console.error('Scan data not found for requestId:', requestId);
    }
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }

  getPiiTypes(): string[] {
    const piiTypes = this.requestDetails?.piiTypes;

    if (
      !piiTypes ||
      piiTypes === 'all' ||
      (Array.isArray(piiTypes) && piiTypes.includes('all'))
    ) {
      return ['all (default)'];
    }

    if (typeof piiTypes === 'string') {
      try {
        // Try parsing JSON array string
        const parsed = JSON.parse(piiTypes);
        if (Array.isArray(parsed)) {
          return parsed;
        }
        return piiTypes.split(',').map((type) => type.trim());
      } catch {
        return piiTypes.split(',').map((type) => type.trim());
      }
    }

    if (Array.isArray(piiTypes)) {
      return piiTypes;
    }

    return [];
  }
  scanFiles() {
    this.router.navigate(['jrm/scan-files-details/files_scan']);
  }

  downloadScanResults(requestId: number): void {
    this.filesScanService.exportScanResults(requestId).subscribe({
      next: (blob: Blob) => {
        this.globalService.downloadFileCommon(
          blob,
          'xlsx',
          'pii-scan-results',
          'blob'
        );
      },
      error: (err) => {
        console.error('Export failed:', err);
      },
    });
  }

exportHtml(request: PiiScanRequest) {
  const comp = this.resultComponents.find(c => c.requestId === request.requestId);
  if (comp) {
    comp.loadAndExportAllResults(request);
  } else {
    console.warn("No child component found for requestId", request.requestId);
  }
}

  startProgressPolling(requestId: number): void {
    const intervalId = setInterval(() => {
      this.filesScanService.getScanProgress(requestId).subscribe({
        next: (progress) => {
          this.progressMap[requestId] = progress;

          if (this.filesScanService.isScanCompleted(progress)) {
            clearInterval(intervalId);
          }

          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(
            `Failed to fetch progress for requestId ${requestId}`,
            err
          );
          clearInterval(intervalId); // Stop polling on error
        },
      });
    }, 3000);
  }
}
