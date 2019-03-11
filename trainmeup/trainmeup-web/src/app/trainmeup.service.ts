import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
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

	constructor(private http: HttpClient) {}

	getCategories(): Observable<Category[]> {
		return this.http.get<Category[]>('http://localhost:8081/api/category');
	}

	saveCategory(category): Observable<Category> {
		return this.http.post<Category>('http://localhost:8081/api/category', category, httpOptions);
	}

	saveQuestion(question): Observable<Question> {
		return this.http.post<Question>('http://localhost:8081/api/question', question, httpOptions);
	}

	hit(categId): Observable<Question> {
		let options = { params: new HttpParams().set('categoryId', categId) };
		return this.http.get<Question>('http://localhost:8081/api/question/hit', options);
	}

	score(questionId, result): Observable<Question> {
		let scoreparam = new HttpParams().set('questionId', questionId).set('guessResult', result);
		return this.http.put<Question>('http://localhost:8081/api/question/score', scoreparam );
	}
	/*private handleError(error: HttpErrorResponse) {
		console.log(error);
		return throwError('Something bad happened; please try again later.');
	}*/
}
