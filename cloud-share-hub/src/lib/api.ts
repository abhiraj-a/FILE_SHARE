// API service for communicating with the backend

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

interface FileEntityDTO {
  fileId: string;
  originalFileName: string;
  fileSize: number;
  uploadedAt: string;
}

interface FileTransferDTO {
  transferId: string;
  verificationCode: string;
  fileCount: number;
  expiresAt?: string;
  fileMetaDataList?: {
    id: string;
    originalFileName: string;
    fileSize: number;
    contentType: string;
  }[];
  downloads?: {
    fileId: string;
    originalFileName: string;
    downloadUrl: string;
  }[];
}

class ApiService {
  private async getAuthHeaders(): Promise<HeadersInit> {
    // Get the token from Clerk - this will be injected from the component
    const token = await (window as any).__getClerkToken?.();
    return {
      'Authorization': token ? `Bearer ${token}` : '',
    };
  }

  async getAllFiles(): Promise<FileEntityDTO[]> {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/files/get-All-Files`, {
      headers,
    });
    if (!response.ok) throw new Error('Failed to fetch files');
    return response.json();
  }

  async uploadFiles(files: File[]): Promise<FileEntityDTO[]> {
    const headers = await this.getAuthHeaders();
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));

    const response = await fetch(`${API_BASE_URL}/files/upload`, {
      method: 'POST',
      headers: {
        ...headers,
      },
      body: formData,
    });
    if (!response.ok) throw new Error('Failed to upload files');
    return response.json();
  }

  async downloadFile(fileId: string): Promise<string> {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/files/download/${fileId}`, {
      headers,
    });
    if (!response.ok) throw new Error('Failed to get download link');
    return response.text();
  }

  async deleteFiles(fileIds: string[]): Promise<void> {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/files`, {
      method: 'DELETE',
      headers: {
        ...headers,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(fileIds),
    });
    if (!response.ok) throw new Error('Failed to delete files');
  }

  async transferFiles(fileIds: string[]): Promise<FileTransferDTO> {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/transfer/files-to-transfer`, {
      method: 'POST',
      headers: {
        ...headers,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(fileIds),
    });
    if (!response.ok) throw new Error('Failed to create transfer');
    return response.json();
  }

  async receiveByCode(code: string): Promise<FileTransferDTO> {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/transfer/receive-via-code/${code}`, {
      headers,
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Invalid code');
      if (response.status === 410) throw new Error('Transfer expired');
      throw new Error('Failed to receive transfer');
    }
    return response.json();
  }

  async downloadTransfer(code: string): Promise<FileTransferDTO> {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/transfer/download-zip/${code}`, {
      headers,
    });
    if (!response.ok) throw new Error('Failed to download transfer');
    return response.json();
  }

  async revokeTransfer(transferId: string): Promise<void> {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/transfer/revoke/${transferId}`, {
      method: 'PUT',
      headers,
    });
    if (!response.ok) throw new Error('Failed to revoke transfer');
  }

  async signup(): Promise<any> {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/user/signup`, {
      method: 'POST',
      headers,
    });
    if (!response.ok) throw new Error('Failed to signup');
    return response.json();
  }

  async requestAccountDeletion(): Promise<void> {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/user/delete-account`, {
      method: 'DELETE',
      headers,
    });
    if (!response.ok) throw new Error('Failed to delete account');
  }
}

export const api = new ApiService();
export type { FileEntityDTO, FileTransferDTO };
