import { Injectable } from '@angular/core';
import { catchError, map, Observable } from 'rxjs';
import { PaginationRequest } from '../models/PaginationRequest';
import { Users } from '../models/Users';
import { ApiService } from './api.service';
import { Stats } from '../models/Status';

@Injectable({
  providedIn: 'root'
})
export class UsersService {

  url: string = '/user';

  constructor(
    private apiService: ApiService
  ) { }

  /**
   * get users list
   * @param body
   * @returns
   */
  userList(body: PaginationRequest): Observable<any> {
    let url = this.url + '/users-list';
    url += '?offset=' + (body.offset ? body.offset : 0);
    url += '&pageSize=' + (body.pageSize ? body.pageSize : '');
    url += '&field=' + (body.field ? body.field : '');
    url += '&sort=' + (body.sort ? body.sort : 0);
    url += '&searchText=' + (body.searchText ? body.searchText : '');
    if (body.status !== undefined) {
      url += '&status=' + body.status;
  }
    return this.apiService.get(url);
  }

  /**
   * view user
   * @param userId
   * @returns
   */
  viewUser(userId: number): Observable<any> {
    return this.apiService.get(this.url + '/users/' + userId);
  }

  /**
 * view user profile
 * @param userId
 * @returns
 */
  viewUserProfile(userId: number): Observable<any> {
    return this.apiService.get(this.url + '/users-profile/' + userId);
  }

  /**
   * add user
   * @param body - Users
   * @returns
   */
  addUser(body: Users): Observable<any> {
    return this.apiService.post(this.url + '/create', body);
  }

  /**
   * edit user profile
   * @param body - Users
   * @returns
   */
  editUserProfile(body: Users): Observable<any> {
    return this.apiService.put(this.url + '/edit-profile/' + body.userId, body);
  }

  /**
 * edit user
 * @param body - Users
 * @returns
 */
  editUser(body: Users): Observable<any> {
    return this.apiService.put(this.url + '/edit-user/' + body.userId, body);
  }


  /**
   * @param userId
   * @returns - user profile image
   */
  getUserProfileImage(userId: number): Observable<any> {
    return this.apiService.get(this.url + '/user-profileImg/' + userId);
  }

    /**
   * Fetches total users, active users, reports, and scans
   * @returns Stats observable
   */
  getStats(): Observable<Stats> {
    return this.apiService.get(this.url + '/stats');
  }


}
