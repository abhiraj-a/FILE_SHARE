import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useAuth } from "@clerk/clerk-react";
import { api, FileEntityDTO } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { 
  Download, 
  Trash2, 
  FileIcon, 
  ArrowLeft,
  Share2,
  RefreshCw,
  FileImage,
  FileVideo,
  FileAudio,
  FileText,
  FileArchive
} from "lucide-react";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";
import { Logo } from "@/components/Logo";
import { NavLink } from "@/components/NavLink";

const getFileIcon = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase() || '';
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'].includes(ext)) return FileImage;
  if (['mp4', 'webm', 'avi', 'mov'].includes(ext)) return FileVideo;
  if (['mp3', 'wav', 'ogg', 'flac'].includes(ext)) return FileAudio;
  if (['pdf', 'doc', 'docx', 'txt', 'md'].includes(ext)) return FileText;
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return FileArchive;
  return FileIcon;
};

const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`;
};

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const MyFiles = () => {
  const navigate = useNavigate();
  const { getToken } = useAuth();
  const [files, setFiles] = useState<FileEntityDTO[]>([]);
  const [selectedFiles, setSelectedFiles] = useState<Set<string>>(new Set());
  const [isLoading, setIsLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isTransferring, setIsTransferring] = useState(false);

  useEffect(() => {
    (window as any).__getClerkToken = async () => {
      return await getToken();
    };
    loadFiles();
    return () => {
      delete (window as any).__getClerkToken;
    };
  }, [getToken]);

  const loadFiles = async () => {
    setIsLoading(true);
    try {
      const data = await api.getAllFiles();
      setFiles(data);
    } catch (error) {
      toast.error("Failed to load files");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownload = async (fileId: string, fileName: string) => {
    try {
      const downloadUrl = await api.downloadFile(fileId);
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      toast.success("Download started!");
    } catch (error) {
      toast.error("Failed to download file");
    }
  };

  const handleDelete = async () => {
    if (selectedFiles.size === 0) return;
    
    setIsDeleting(true);
    try {
      await api.deleteFiles(Array.from(selectedFiles));
      setFiles(files.filter(f => !selectedFiles.has(f.fileId)));
      setSelectedFiles(new Set());
      toast.success(`Deleted ${selectedFiles.size} file(s)`);
    } catch (error) {
      toast.error("Failed to delete files");
    } finally {
      setIsDeleting(false);
    }
  };

  const handleShare = async () => {
    if (selectedFiles.size === 0) return;
    
    setIsTransferring(true);
    try {
      const transfer = await api.transferFiles(Array.from(selectedFiles));
      toast.success(`Transfer created! Code: ${transfer.verificationCode}`);
      navigate(`/?transfer=${transfer.verificationCode}`);
    } catch (error) {
      toast.error("Failed to create transfer");
    } finally {
      setIsTransferring(false);
    }
  };

  const toggleSelect = (fileId: string) => {
    const newSelected = new Set(selectedFiles);
    if (newSelected.has(fileId)) {
      newSelected.delete(fileId);
    } else {
      newSelected.add(fileId);
    }
    setSelectedFiles(newSelected);
  };

  const toggleSelectAll = () => {
    if (selectedFiles.size === files.length) {
      setSelectedFiles(new Set());
    } else {
      setSelectedFiles(new Set(files.map(f => f.fileId)));
    }
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Background Effects */}
      <div className="fixed inset-0 pointer-events-none">
        <div className="absolute top-0 left-1/4 w-96 h-96 bg-primary/5 rounded-full blur-3xl" />
        <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-primary/5 rounded-full blur-3xl" />
      </div>

      {/* Header */}
      <header className="relative z-10 border-b border-border/50">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <Logo />
            <nav className="flex items-center gap-4">
              <NavLink to="/" icon={ArrowLeft}>Home</NavLink>
            </nav>
          </div>
          <Button variant="ghost" size="icon" onClick={loadFiles} disabled={isLoading}>
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
          </Button>
        </div>
      </header>

      {/* Main Content */}
      <main className="relative z-10 container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="mb-8"
          >
            <h1 className="text-3xl font-bold mb-2">My Files</h1>
            <p className="text-muted-foreground">
              Manage your uploaded files
            </p>
          </motion.div>

          {/* Action Bar */}
          <AnimatePresence>
            {selectedFiles.size > 0 && (
              <motion.div
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                className="glass rounded-xl p-4 mb-6 flex items-center justify-between"
              >
                <span className="text-sm text-muted-foreground">
                  {selectedFiles.size} file(s) selected
                </span>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={handleShare}
                    disabled={isTransferring}
                    className="gap-2"
                  >
                    <Share2 className="w-4 h-4" />
                    Share
                  </Button>
                  <Button
                    variant="destructive"
                    size="sm"
                    onClick={handleDelete}
                    disabled={isDeleting}
                    className="gap-2"
                  >
                    <Trash2 className="w-4 h-4" />
                    Delete
                  </Button>
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Files List */}
          <div className="glass rounded-2xl overflow-hidden">
            {isLoading ? (
              <div className="p-12 flex items-center justify-center">
                <motion.div
                  animate={{ rotate: 360 }}
                  transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                  className="w-8 h-8 border-2 border-primary/30 border-t-primary rounded-full"
                />
              </div>
            ) : files.length === 0 ? (
              <div className="p-12 text-center">
                <FileIcon className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">No files uploaded yet</p>
                <Button
                  variant="send"
                  className="mt-4"
                  onClick={() => navigate('/')}
                >
                  Upload Files
                </Button>
              </div>
            ) : (
              <>
                {/* Table Header */}
                <div className="border-b border-border/50 px-4 py-3 flex items-center gap-4 bg-muted/30">
                  <Checkbox
                    checked={selectedFiles.size === files.length && files.length > 0}
                    onCheckedChange={toggleSelectAll}
                  />
                  <span className="flex-1 text-sm font-medium">Name</span>
                  <span className="w-24 text-sm font-medium text-right hidden sm:block">Size</span>
                  <span className="w-40 text-sm font-medium text-right hidden md:block">Uploaded</span>
                  <span className="w-20"></span>
                </div>

                {/* Files */}
                <div className="divide-y divide-border/30">
                  {files.map((file, index) => {
                    const FileIconComponent = getFileIcon(file.originalFileName);
                    return (
                      <motion.div
                        key={file.fileId}
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: index * 0.05 }}
                        className={`px-4 py-3 flex items-center gap-4 hover:bg-muted/20 transition-colors ${
                          selectedFiles.has(file.fileId) ? 'bg-primary/5' : ''
                        }`}
                      >
                        <Checkbox
                          checked={selectedFiles.has(file.fileId)}
                          onCheckedChange={() => toggleSelect(file.fileId)}
                        />
                        <div className="flex items-center gap-3 flex-1 min-w-0">
                          <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                            <FileIconComponent className="w-5 h-5 text-primary" />
                          </div>
                          <span className="truncate font-medium">{file.originalFileName}</span>
                        </div>
                        <span className="w-24 text-sm text-muted-foreground text-right hidden sm:block">
                          {formatFileSize(file.fileSize)}
                        </span>
                        <span className="w-40 text-sm text-muted-foreground text-right hidden md:block">
                          {formatDate(file.uploadedAt)}
                        </span>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleDownload(file.fileId, file.originalFileName)}
                          className="flex-shrink-0"
                        >
                          <Download className="w-4 h-4" />
                        </Button>
                      </motion.div>
                    );
                  })}
                </div>
              </>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default MyFiles;
