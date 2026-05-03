import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-concierge',
  templateUrl: './concierge.component.html',
  styleUrls: ['./concierge.component.css']
})
export class ConciergeComponent {
  selectedFile: File | null = null;
  selectedImage: string | null = null;
  selectedCategory: string = 'Similar';
  categories: string[] = ['Similar', 'Ring', 'Bracelet', 'Necklace', 'Earring'];
  results: any[] = [];
  loading: boolean = false;
  apiUrl: string = environment.apiUrl;

  constructor(private http: HttpClient) {}

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = (e: any) => this.selectedImage = e.target.result;
      reader.readAsDataURL(file);
    }
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    const file = event.dataTransfer?.files[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = (e: any) => this.selectedImage = e.target.result;
      reader.readAsDataURL(file);
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
  }

  search() {
    if (!this.selectedFile) return;

    this.loading = true;
    const formData = new FormData();
    formData.append('image', this.selectedFile);
    
    let endpoint = '/api/jewelry/search';
    
    if (this.selectedCategory !== 'Similar') {
      endpoint = '/api/jewelry/pair';
      formData.append('category', this.selectedCategory);
      formData.append('text', `Find a matching ${this.selectedCategory}`);
    }

    this.http.post<any[]>(`${environment.apiUrl}${endpoint}`, formData)
      .subscribe({
        next: (res) => {
          this.results = res;
          this.loading = false;
        },
        error: (err) => {
          console.error(err);
          this.loading = false;
        }
      });
  }
}
