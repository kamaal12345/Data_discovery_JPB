import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-properties',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './properties.component.html',
  styleUrl: './properties.component.css',
})
export class PropertiesComponent implements OnInit {
  constructor() {}

  ngOnInit(): void {}
}
