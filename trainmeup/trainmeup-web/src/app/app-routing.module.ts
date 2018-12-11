import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent }      from './dashboard/dashboard.component';
import { QuestionComponent }      from './question/question.component';
import { AnswerComponent }      from './answer/answer.component';
import { CategoriesComponent }      from './categories/categories.component';
/*import { HomeComponent } from './pages/home/home.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { DisplayDataComponent } from './pages/display-data/display-data.component';*/
import { DxDataGridModule, DxFormModule } from 'devextreme-angular';


const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'answer', component: AnswerComponent },
  { path: 'newquestion', component: QuestionComponent },
  { path: 'categories', component: CategoriesComponent }
/*, {
        path: 'home',
        component: HomeComponent
    }, {
        path: 'profile',
        component: ProfileComponent
    }, {
        path: 'display-data',
        component: DisplayDataComponent
    }*/
    ];

@NgModule({
  exports: [ RouterModule ],
  //declarations: [HomeComponent, ProfileComponent, DisplayDataComponent],
  imports: [
    CommonModule,
    RouterModule.forRoot(routes),
    DxDataGridModule,
    DxFormModule
  ]
})
export class AppRoutingModule { }
