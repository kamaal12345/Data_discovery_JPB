import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { catchError, map, Observable } from 'rxjs';
import { Host } from '../models/Host';
import { PaginationRequest } from '../models/PaginationRequest';

@Injectable({
  providedIn: 'root',
})
export class HostsService {
  url: string = '/hosts';

  constructor(private apiService: ApiService) {}

  /**
   * get all host list
   * @returns
   */
  getHostsList(): Observable<any> {
    return this.apiService.get(this.url + '/get_all_hosts');
  }

    /**
   * get only Active host list
   * @returns
   */
  getAllActiveHostsList(): Observable<any> {
    return this.apiService.get(this.url + '/get_active_hosts');
  }

  /**
   * get Host data List with filters
   * @returns
   */
  // getAllHostList(body: PaginationRequest): Observable<any> {
  //   let url = this.url + '/all';
  //   url += '?offset=' + (body.offset ? body.offset : 0);
  //   url += '&pageSize=' + (body.pageSize ? body.pageSize : '');
  //   url += '&field=' + (body.field ? body.field : '');
  //   url += '&sort=' + (body.sort ? body.sort : 0);
  //   url += '&search' + (body.searchText ? body.searchText : '');
  //   return this.apiService.get(url).pipe(
  //     map((res) => {
  //       return <any>res;
  //     }),
  //     catchError((err) => this.apiService.handleError(err))
  //   );
  // }

  /**
   * view only id
   * @param id
   * @returns
   */
  getHostData(id: number): Observable<any> {
    return this.apiService.get(this.url + '/get_host/' + id);
  }

  /**
   * add only Hosts
   * @param body
   * @returns
   */
  addHosts(body: Host): Observable<any> {
    return this.apiService.post(this.url + '/add_host', body);
  }

  /**
   * edit
   * @param body - Users
   * @returns
   */
  editHosts(body: Host): Observable<any> {
    return this.apiService.put(this.url + '/update_host/' + body.id, body);
  }
}
