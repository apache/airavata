import {Injectable} from "@angular/core";
import {Http, Headers, Response} from "@angular/http";
import {Observable} from "rxjs";

/**
 * Created by dimuthu on 10/29/17.
 */

@Injectable()
export class ApiService {

  public baseUrl = "http://localhost:8080/";

  constructor(private http:Http) {
  }

  public get(url, headers:Headers = new Headers()): Observable<Response> {

    this.createHeader(headers);
    return this.http.get(this.baseUrl + url, {headers: headers});
  }

  public post(url, body, headers:Headers = new Headers()): Observable<Response> {

    this.createHeader(headers);
    return this.http.post(this.baseUrl + url, body, {headers: headers});
  }

  public put(url, body, headers:Headers = new Headers()): Observable<Response> {

    this.createHeader(headers);
    return this.http.put(this.baseUrl + url, body, {headers: headers});
  }

  public delete(url, headers:Headers = new Headers()): Observable<Response> {

    this.createHeader(headers);
    return this.http.delete(encodeURI(this.baseUrl + url), {headers: headers});
  }

  private createHeader(headers:Headers) {
    if (!headers.has("Content-Type")) {
      headers.append("Content-Type", "application/json");
    }
  }
}
