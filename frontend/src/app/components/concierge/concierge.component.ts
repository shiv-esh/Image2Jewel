import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-concierge',
  templateUrl: './concierge.component.html',
  styleUrls: ['./concierge.component.css']
})
export class ConciergeComponent {
  selectedFile: File | null = null;
  selectedImage: string | null = null;
  queryText: string = '';
  results: any[] = [];
  loading: boolean = false;

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
    if (this.queryText.toLowerCase().includes('match') || this.queryText.toLowerCase().includes('pair')) {
      endpoint = '/api/jewelry/pair';
      formData.append('category', this.extractCategory(this.queryText));
      formData.append('text', this.queryText);
    }

    this.http.post<any[]>(`http://localhost:8080${endpoint}`, formData)
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

  private extractCategory(text: string): string {
    if (text.toLowerCase().includes('earring')) return 'Earrings';
    if (text.toLowerCase().includes('necklace')) return 'Necklace';
    if (text.toLowerCase().includes('ring')) return 'Ring';
    return 'Jewelry';
  }
}
