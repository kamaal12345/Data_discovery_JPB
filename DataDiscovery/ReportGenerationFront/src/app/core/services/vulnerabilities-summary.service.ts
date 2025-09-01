import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';
import { PaginationRequest } from '../models/PaginationRequest';
import { ApiService } from './api.service';
import { VulnerabilitiesSummary } from '../models/VulnerabilitiesSummary';

@Injectable({
  providedIn: 'root'
})
export class VulnerabilitiesSummaryService {

  apiUrl: string = environment.api_url;
  url: string = '/vulnerabilities';
  
  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) { }


  /**
   * Creates a new vulnerability summary.
   * @param summary The vulnerability summary object to create.
   * @returns An Observable of the created summary.
   */
  createSummary(summary: VulnerabilitiesSummary): Observable<VulnerabilitiesSummary> {
    return this.http.post<VulnerabilitiesSummary>(this.apiUrl, summary);
  }

  /**
   * Fetches all vulnerability summaries.
   * @returns An Observable array of vulnerability summaries.
   */
  getAllSummaries(): Observable<VulnerabilitiesSummary[]> {
    return this.http.get<VulnerabilitiesSummary[]>(this.apiUrl);
  }

  getAllSummariesList(body: PaginationRequest): Observable<any> {
    let url = this.apiUrl + this.url+ '/vul_list';
    url += '?offset=' + (body.offset ? body.offset : 0);
    url += '&pageSize=' + (body.pageSize ? body.pageSize : '');
    url += '&field=' + (body.field ? body.field : '');
    url += '&sort=' + (body.sort ? body.sort : 0);
    url += '&searchText=' + (body.searchText ? body.searchText : '');
    url += '&startDate=' + (body.startDate ? body.startDate : '');
    url += '&endDate=' + (body.endDate ? body.endDate : '');
    return this.http.get(url)
    .pipe(
      map(res => {
        return <any>res;
      }),
      catchError(err => this.apiService.handleError(err)));
  }

  /**
   * Fetches a specific vulnerability summary by ID.
   * @param id The ID of the summary to retrieve.
   * @returns An Observable of the vulnerability summary.
   */
  getSummaryById(id: number): Observable<VulnerabilitiesSummary> {
    return this.http.get<VulnerabilitiesSummary>(`${this.apiUrl}/${id}`);
  }

  /**
   * Updates a vulnerability summary by ID.
   * @param id The ID of the summary to update.
   * @param updatedSummary The updated summary object.
   * @returns An Observable of the updated summary.
   */
  updateSummary(id: number, updatedSummary: VulnerabilitiesSummary): Observable<VulnerabilitiesSummary> {
    return this.http.put<VulnerabilitiesSummary>(`${this.apiUrl}/${id}`, updatedSummary);
  }

  /**
   * Deletes a vulnerability summary by ID.
   * @param id The ID of the summary to delete.
   * @returns An Observable of void.
   */
  deleteSummary(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
