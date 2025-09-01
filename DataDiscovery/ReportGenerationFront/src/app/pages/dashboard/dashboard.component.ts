import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../../shared/shared.module';
import { UsersService } from '../../core/services/users.service';
import { Stats } from '../../core/models/Status';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  EmployeeCount: number = 0;
  reportCount: number = 0;
  ScansCount: number = 0;

  constructor(private userService: UsersService) {}

  ngOnInit(): void {
    this.userService.getStats().subscribe({
      next: (data: Stats) => {
        this.EmployeeCount = data.totalUsers;
        this.reportCount = data.reportCount;
        this.ScansCount = data.scanCount;
      },
      error: (err) => {
        console.error('Failed to load stats:', err);
      }
    });
  }
}
