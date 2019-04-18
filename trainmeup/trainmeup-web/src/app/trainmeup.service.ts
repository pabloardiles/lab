import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../environments/environment';

export class Category {
    id?: string;
    categoryId: string;
    parentId?: string;
    name: string;
    nextQuestionId: string;
    nextCategoryId: string;
    expanded?: boolean;
}

export class Question {
   id?: string;
   questionId: string;
   question: string;
   answer: string;
   parentId: string;
   createDate: string;
   updateDate: string;
   rank: string;
   attempts: number;
}

const httpOptions = {
	  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
	};

@Injectable({
  providedIn: 'root'
})
export class TrainmeupService {

	private apiURL: string = '';

	constructor(private http: HttpClient) {
		this.apiURL = environment.tmupApiURL;
	}

	getCategories(): Observable<Category[]> {
		return this.http.get<Category[]>(this.apiURL + '/api/category');
	}

	saveCategory(category): Observable<Category> {
		return this.http.post<Category>(this.apiURL + '/api/category', category, httpOptions);
	}

	saveQuestion(question): Observable<Question> {
		return this.http.post<Question>(this.apiURL + '/api/question', question, httpOptions);
	}

	hit(categId): Observable<Question> {
		let options = { params: new HttpParams().set('categoryId', categId) };
		return this.http.get<Question>(this.apiURL + '/api/question/hit', options);
	}

	score(questionId, result): Observable<Question> {
		let scoreparam = new HttpParams().set('questionId', questionId).set('guessResult', result);
		return this.http.put<Question>(this.apiURL + '/api/question/score', scoreparam );
	}

	test(): Observable<any> {
		return this.http.post<any>(this.apiURL + '/api/health', httpOptions);
	}

	getPath(categoryId: string): Observable<any> {
		let param = { params: new HttpParams().set('categoryId', categoryId) };
		return this.http.get<any>(this.apiURL + '/api/path', param);
	}

	/*private handleError(error: HttpErrorResponse) {
		console.log(error);
		return throwError('Something bad happened; please try again later.');
	}*/
}
