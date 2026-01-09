import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { useAuth } from "@clerk/clerk-react";
import { useNavigate } from "react-router-dom";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { FileUploadZone } from "@/components/FileUploadZone";
import { Logo } from "@/components/Logo";
import { NavLink } from "@/components/NavLink";
import { ArrowLeft, Upload, CheckCircle } from "lucide-react";
import { toast } from "sonner";

const UploadPage = () => {
  const navigate = useNavigate();
  const { getToken } = useAuth();
  const [files, setFiles] = useState<File[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadComplete, setUploadComplete] = useState(false);

  useEffect(() => {
    (window as any).__getClerkToken = async () => {
      return await getToken();
    };
    return () => {
      delete (window as any).__getClerkToken;
    };
  }, [getToken]);

  const handleUpload = async () => {
    if (files.length === 0) return;
    
    setIsUploading(true);
    try {
      await api.uploadFiles(files);
      setUploadComplete(true);
      toast.success(`${files.length} file(s) uploaded successfully!`);
      setTimeout(() => {
        navigate('/my-files');
      }, 1500);
    } catch (error) {
      toast.error("Failed to upload files. Please try again.");
    } finally {
      setIsUploading(false);
    }
  };

  const resetUpload = () => {
    setFiles([]);
    setUploadComplete(false);
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
        </div>
      </header>

      {/* Main Content */}
      <main className="relative z-10 container mx-auto px-4 py-12">
        <div className="max-w-xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-8"
          >
            <h1 className="text-3xl font-bold mb-2">Upload Files</h1>
            <p className="text-muted-foreground">
              Upload files to your cloud storage for later sharing
            </p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="glass rounded-3xl p-6 md:p-8"
          >
            {uploadComplete ? (
              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                className="text-center py-8 space-y-4"
              >
                <motion.div
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ type: "spring", delay: 0.2 }}
                  className="w-20 h-20 mx-auto rounded-full bg-success/20 flex items-center justify-center"
                >
                  <CheckCircle className="w-10 h-10 text-success" />
                </motion.div>
                <h3 className="text-xl font-semibold">Upload Complete!</h3>
                <p className="text-muted-foreground">Redirecting to My Files...</p>
              </motion.div>
            ) : (
              <div className="space-y-6">
                <FileUploadZone 
                  files={files} 
                  setFiles={setFiles}
                  isUploading={isUploading}
                />
                
                {files.length > 0 && (
                  <div className="flex gap-3">
                    <Button
                      onClick={resetUpload}
                      variant="outline"
                      className="flex-1"
                      disabled={isUploading}
                    >
                      Clear
                    </Button>
                    <Button
                      onClick={handleUpload}
                      variant="send"
                      size="lg"
                      className="flex-1 gap-2"
                      disabled={isUploading}
                    >
                      {isUploading ? (
                        <motion.div
                          animate={{ rotate: 360 }}
                          transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                          className="w-5 h-5 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full"
                        />
                      ) : (
                        <>
                          <Upload className="w-5 h-5" />
                          Upload {files.length} file{files.length !== 1 ? 's' : ''}
                        </>
                      )}
                    </Button>
                  </div>
                )}
              </div>
            )}
          </motion.div>
        </div>
      </main>
    </div>
  );
};

export default UploadPage;
