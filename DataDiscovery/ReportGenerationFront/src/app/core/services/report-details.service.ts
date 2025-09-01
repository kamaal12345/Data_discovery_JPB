import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError } from 'rxjs';
import { environment } from '../../../environments/environment.prod';
import { PaginationRequest } from '../models/PaginationRequest';

@Injectable({
  providedIn: 'root',
})
export class ReportDetailsService {
  url: string = '/reports';

  constructor(private apiService: ApiService) {}

  /**
   * get only Report list
   * @returns
   */
  reportList(): Observable<any> {
    return this.apiService.get(this.url + '/list_report_details');
  }

  /**
   * view only report
   * @param reportId
   * @returns
   */
  getReport(reportId: number): Observable<any> {
    return this.apiService.get(this.url + '/' + reportId);
  }

  /**
   * add only report Details
   * @param body
   * @returns
   */
  addReport(body: any): Observable<any> {
    return this.apiService.post(this.url + '/add_report_details', body);
  }

  /**
   * add report Details and list vulnerabilities
   * @param body
   * @returns
   */
  addReportDetailsVulnerabilitiesFUI(body: any): Observable<any> {
    return this.apiService.post(this.url + '/add_report_details', body);
  }

    /**
   * add report Details and list vulnerabilities from excel
   * @param body
   * @returns
   */
  addReportDetailsVulnerabilitiesFExcel(body: any): Observable<any> {
    return this.apiService.post(this.url + '/add_report_details_f_excel', body);
  }

  /**
   * edit report Details  with vulnerabilities
   * @param body
   * @returns
   */
  editReportDetailsWithVulnerabilitiesFUI(body: any): Observable<any> {
    return this.apiService.put(this.url + '/update_report_details',body);
  }

  /**
   * get report with Vulnerabilities List with filters
   * @returns
   */
  getAllReportDetailsList(body: PaginationRequest): Observable<any> {
    let url = this.url+ '/reportDetails-list';
    url += '?offset=' + (body.offset ? body.offset : 0);
    url += '&pageSize=' + (body.pageSize ? body.pageSize : '');
    url += '&field=' + (body.field ? body.field : '');
    url += '&sort=' + (body.sort ? body.sort : 0);
    return this.apiService.get(url)
      .pipe(
        map(res => {
          return <any>res;
        }),
        catchError(err => this.apiService.handleError(err)));
  }

    /**
   * Check if a report with the same scope already exists.
   * @param scope - The scope to check for duplicates.
   * @returns Observable with the response containing `isDuplicate` and `reportId`
   */
  checkScope(scope: string): Observable<any> {
    const params = new HttpParams().set('scope', scope);

    return this.apiService.get(this.url +'/check-scope', params).pipe(
      map(response => {
        return response;
      }),
      catchError(error => {
        // Handle error
        console.error('Error checking scope:', error);
        throw new Error('Error checking scope');
      })
    );
  }

  getTotalReportData(reportId: number): Observable<any> {
    return this.apiService.get(this.url + '/total/' + reportId
    );
  }
}
