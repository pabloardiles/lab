import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export class Category {
    id?: string;
    categoryId: string;
    parentId?: string;
    name: string;
    nextQuestionId: string;
    nextCategoryId: string;
    expanded?: boolean;
}

const httpOptions = {
	  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
	};


@Injectable({
  providedIn: 'root'
})
export class TrainmeupService {

	constructor(private http: HttpClient) {}

	getCategories(): Observable<Category[]> {
		return this.http.get<Category[]>('http://localhost:8081/api/category');
	}

	save(category): Observable<Category> {
		return this.http.post<Category>('http://localhost:8081/api/category', category, httpOptions);
	}

	/*private handleError(error: HttpErrorResponse) {
		console.log(error);
		return throwError('Something bad happened; please try again later.');
	}*/
}
