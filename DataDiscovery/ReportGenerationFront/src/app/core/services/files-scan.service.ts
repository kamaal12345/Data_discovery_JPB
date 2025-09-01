import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ApiService } from './api.service';
import { PaginationRequest } from '../models/PaginationRequest';
import { catchError, map, Observable } from 'rxjs';
import { PiiScanRequest } from '../models/PiiScanRequest';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root',
})
export class FilesScanService {
  url: string = '/pii-scan';
  constructor(private apiService: ApiService,private tokenService: TokenService,) {}

  /**
   * get Scan data List with filters
   * @returns
   */
  getAllScanDataList(body: PaginationRequest): Observable<any> {
    let url = this.url + '/scan_list';
    url += '?offset=' + (body.offset ? body.offset : 0);
    url += '&pageSize=' + (body.pageSize ? body.pageSize : '');
    url += '&field=' + (body.field ? body.field : '');
    url += '&sort=' + (body.sort ? body.sort : 0);
    url += '&search' + (body.searchText ? body.searchText : '');
    return this.apiService.get(url).pipe(
      map((res) => {
        return <any>res;
      }),
      catchError((err) => this.apiService.handleError(err))
    );
  }

  /**
   * add scan data
   * @param body
   * @returns
   */
addRemoteScanData(body: any): Observable<any> {
  const refreshToken = this.tokenService.getRefreshToken();
  const headers = new HttpHeaders({
    'Content-Type': 'application/json',
    'Authorization': refreshToken || ''
  });

  return this.apiService.postWithCustomHeaders(this.url +'/remote-scan', body, headers);
}



/**
 * Export scanned PII results to Excel by requestId
 * @param requestId
 * @returns Blob (Excel file)
 */
exportScanResults(requestId: number): Observable<Blob> {
  return this.apiService.downloadFile(this.url + '/export-pii-results/' + requestId);
}


/**
 * Get real-time scan progress by request ID
 * @param requestId
 * @returns progress { scannedFiles, totalFiles, percentage }
 */
getScanProgress(requestId: number): Observable<any> {
  return this.apiService.get('/api/scan/progress/' + requestId);
}


isScanCompleted(progress: any): boolean {
  return progress.totalFiles > 0 && progress.scannedFiles >= progress.totalFiles;
}

getResultsByRequestId(requestId: number, pagination: PaginationRequest): Observable<any> {
  let url = '/pii-scanres/' + requestId + '/results';
  url += '?page=' + pagination.offset;
  url += '&size=' + pagination.pageSize;

  if (pagination.searchText && pagination.searchText.trim() !== '') {
    url += '&searchText=' + encodeURIComponent(pagination.searchText.trim());
  }

  return this.apiService.get(url).pipe(
    map((res) => res as any),
    catchError(this.apiService.handleError)
  );
}



/**
 * Get ALL scan results for a given requestId (no pagination)
 * @param requestId
 * @returns Observable<PiiScanResult[]>
 */
getAllResultsByRequestId(requestId: number): Observable<any[]> {
  const url = '/pii-scanres/' + requestId + '/all-results';
  return this.apiService.get(url).pipe(
    map((res) => res as any[]),
    catchError(this.apiService.handleError)
  );
}




}
