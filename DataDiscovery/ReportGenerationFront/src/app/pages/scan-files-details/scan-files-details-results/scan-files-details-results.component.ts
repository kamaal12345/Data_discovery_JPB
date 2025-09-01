import {
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import { finalize, Subject, takeUntil } from 'rxjs';
import { PaginationRequest } from '../../../core/models/PaginationRequest';
import { PaginationResponseData } from '../../../core/models/PaginationResponseData';
import { PiiScanResult } from '../../../core/models/PiiScanResult';
import { GlobalService } from '../../../core/services/global-service.service';
import { FilesScanService } from '../../../core/services/files-scan.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';
import { SharedModule } from '../../../../shared/shared.module';
import { PiiScanRequest } from '../../../core/models/PiiScanRequest';

@Component({
  selector: 'app-scan-files-details-results',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './scan-files-details-results.component.html',
  styleUrl: './scan-files-details-results.component.css',
})
export class ScanFilesDetailsResultsComponent implements OnInit, OnChanges, OnDestroy {
  @Input() requestId!: number;
  @Input() searchText: string = '';

  isLoading = false;
  destroy$ = new Subject<boolean>();

  paginationRequest: PaginationRequest = {
    offset: 0,
    pageSize: 20,
    field: 'createdDate',
    sort: 1,
    searchText: '',
    startDate: null,
    endDate: null,
    status: ''
  };

  paginationResponseData!: PaginationResponseData;
  scanResultList: PiiScanResult[] = [];
  totalResult = 0;
  page = 1;
  pageLimits: number[] = [];

  constructor(
  public globalService: GlobalService,
  public filesScanService: FilesScanService,
  private spinner: NgxUiLoaderService,
  private cdr: ChangeDetectorRef
  ) {
     this.pageLimits = this.globalService.getPageLimits();
  }

  ngOnInit(): void {
    if (this.requestId) {
      this.getFileScanResultList();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['requestId'] && this.requestId) || changes['searchText']) {
      this.resetPageNumber();
      this.paginationRequest.searchText = this.searchText || '';
      this.getFileScanResultList();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  resetPageNumber(): void {
    this.page = 1;
    this.paginationRequest.offset = 0;
  }

  onPageChange(event: number): void {
    if (event) {
      this.paginationRequest.offset = event - 1;
      this.getFileScanResultList();
    }
  }

  onPageLimitChange(event: Event): void {
    const value = +(event.target as HTMLInputElement).value;
    this.paginationRequest.pageSize = value;
    this.resetPageNumber();
    this.getFileScanResultList();
  }

  getFileScanResultList(): void {
    this.isLoading = true;
    this.spinner.start();

    this.filesScanService
      .getResultsByRequestId(this.requestId, this.paginationRequest)
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
          this.totalResult = data?.totalElements || 0;
          this.scanResultList = data?.content || [];
          this.cdr.detectChanges();
        },
        error: (e) => console.error(e)
      });
  }

loadAndExportAllResults(request:PiiScanRequest): void {
  this.filesScanService.getAllResultsByRequestId(request.requestId).subscribe({
    next: (results) => {
      this.scanResultList = results;
      this.exportToHtml(request);
    },
    error: (err) => {
      console.error('Failed to load all results:', err);
    }
  });
}


exportToHtml(request:PiiScanRequest): void {
  if (!this.scanResultList || this.scanResultList.length === 0) {
    console.warn('No results available to export.');
    return;
  }

  // Build table rows
  const rows = this.scanResultList
    .map(
      (r: PiiScanResult, i: number) => `
        <tr>
          <td>${i + 1}</td>
          <td>${r.piiType}</td>
          <td>${r.matchedData}</td>
          <td>${r.ip}</td>
          <td>${r.filePath}</td>
        </tr>
      `
    )
    .join('');

  // Full HTML
  const htmlContent = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>PII Scan Results</title>
      <style>
        body { font-family: Arial, sans-serif; margin: 20px; font-size: 12px; }
        h2 { color: #333; font-size: 14px; }
        p { font-size: 12px; margin: 4px 0; }
        table { border-collapse: collapse; width: 100%; margin-top: 10px; font-size: 12px; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
        th { background: #0f1530; color: white; }
        tr:nth-child(even) { background: #f9f9f9; }
      </style>
    </head>
    <body>
      <h2>PII Scan Results</h2>
      <p><strong>Request ID:</strong> ${request.requestId}</p>
      <p><strong>Target Name:</strong> ${request?.targetName ?? ''}</p>

      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>PII Type</th>
            <th>Matched Data</th>
            <th>Host/IP</th>
            <th>File Path</th>
          </tr>
        </thead>
        <tbody>
          ${rows}
        </tbody>
      </table>
    </body>
    </html>
  `;

  // Trigger download
  const blob = new Blob([htmlContent], { type: 'text/html' });
  const url = URL.createObjectURL(blob);

  const a = document.createElement('a');
  a.href = url;
  a.download = `pii-scan-results-${this.requestId}.html`;

  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);

  URL.revokeObjectURL(url);
}


}