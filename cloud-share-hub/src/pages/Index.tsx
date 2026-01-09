import { useState, useCallback, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { SignInButton, SignUpButton, SignOutButton, useUser, useAuth } from "@clerk/clerk-react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { Logo } from "@/components/Logo";
import { ModeSelector } from "@/components/ModeSelector";
import { FileSourceSelector } from "@/components/FileSourceSelector";
import { VerificationCodeDisplay } from "@/components/VerificationCodeDisplay";
import { CodeInput } from "@/components/CodeInput";
import { FileList } from "@/components/FileList";
import { Button } from "@/components/ui/button";
import { api, FileTransferDTO, FileEntityDTO } from "@/lib/api";
import { Send, LogOut, Folder, Settings, X, Upload } from "lucide-react";
import { toast } from "sonner";
import { NavLink } from "@/components/NavLink";

type Mode = "send" | "receive" | null;
type SendStep = "select" | "code";

const Dashboard = () => {
  const { user } = useUser();
  const { getToken } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const [mode, setMode] = useState<Mode>(null);
  const [sendStep, setSendStep] = useState<SendStep>("select");
  const [selectedCloudFiles, setSelectedCloudFiles] = useState<FileEntityDTO[]>([]);
  const [localFiles, setLocalFiles] = useState<File[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [transferData, setTransferData] = useState<FileTransferDTO | null>(null);
  const [receiveError, setReceiveError] = useState<string | null>(null);
  const [receivedFiles, setReceivedFiles] = useState<FileTransferDTO | null>(null);
  const [showTransferPopup, setShowTransferPopup] = useState(false);

  // Check for transfer code from URL
  useEffect(() => {
    const transferCode = searchParams.get('transfer');
    if (transferCode) {
      setMode('send');
      setSendStep('code');
      setTransferData({ 
        verificationCode: transferCode, 
        transferId: '', 
        fileCount: 0 
      });
      setShowTransferPopup(true);
      setSearchParams({});
    }
  }, [searchParams, setSearchParams]);

  // Set up auth token getter for API
  useEffect(() => {
    (window as any).__getClerkToken = async () => {
      return await getToken();
    };
    
    // Call signup on first load
    api.signup().catch(() => {});
    
    return () => {
      delete (window as any).__getClerkToken;
    };
  }, [getToken]);

  const handleSend = async () => {
    const totalSelected = selectedCloudFiles.length + localFiles.length;
    if (totalSelected === 0) return;
    
    setIsLoading(true);
    try {
      let fileIdsToTransfer: string[] = [];
      
      // Upload local files first if any
      if (localFiles.length > 0) {
        const uploadedFiles = await api.uploadFiles(localFiles);
        fileIdsToTransfer = uploadedFiles.map(f => f.fileId);
      }
      
      // Add selected cloud files
      fileIdsToTransfer = [...fileIdsToTransfer, ...selectedCloudFiles.map(f => f.fileId)];
      
      // Create transfer
      const transfer = await api.transferFiles(fileIdsToTransfer);
      setTransferData(transfer);
      setSendStep("code");
      toast.success("Transfer created successfully!");
    } catch (error) {
      toast.error("Failed to create transfer. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleReceive = async (code: string) => {
    setIsLoading(true);
    setReceiveError(null);
    try {
      const transfer = await api.receiveByCode(code);
      setReceivedFiles(transfer);
    } catch (error: any) {
      setReceiveError(error.message || "Invalid or expired code");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownloadAll = async () => {
    if (!receivedFiles?.verificationCode) return;
    
    setIsLoading(true);
    try {
      const transfer = await api.downloadTransfer(receivedFiles.verificationCode);
      
      // Download each file
      if (transfer.downloads) {
        for (const download of transfer.downloads) {
          const link = document.createElement('a');
          link.href = download.downloadUrl;
          link.download = download.originalFileName;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
        }
        toast.success("Downloads started!");
      }
    } catch (error) {
      toast.error("Failed to download files");
    } finally {
      setIsLoading(false);
    }
  };

  const resetSend = () => {
    setSelectedCloudFiles([]);
    setLocalFiles([]);
    setSendStep("select");
    setTransferData(null);
  };

  const resetReceive = () => {
    setReceivedFiles(null);
    setReceiveError(null);
  };

  const handleModeChange = (newMode: Mode) => {
    setMode(newMode);
    if (newMode === null) {
      resetSend();
      resetReceive();
    }
  };

  return (
    <div className="min-h-screen bg-background relative overflow-hidden">
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
            <nav className="hidden sm:flex items-center gap-4">
              <NavLink to="/upload" icon={Upload}>Upload</NavLink>
              <NavLink to="/my-files" icon={Folder}>My Files</NavLink>
              <NavLink to="/settings" icon={Settings}>Settings</NavLink>
            </nav>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full gradient-primary flex items-center justify-center text-sm font-semibold text-primary-foreground">
                {user?.firstName?.[0] || user?.emailAddresses?.[0]?.emailAddress?.[0]?.toUpperCase()}
              </div>
              <span className="text-sm text-muted-foreground hidden sm:block">
                {user?.firstName || user?.emailAddresses?.[0]?.emailAddress}
              </span>
            </div>
            <SignOutButton>
              <Button variant="ghost" size="icon">
                <LogOut className="w-4 h-4" />
              </Button>
            </SignOutButton>
          </div>
        </div>
      </header>

      {/* Transfer Success Popup */}
      <AnimatePresence>
        {showTransferPopup && transferData && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-background/80 backdrop-blur-sm flex items-center justify-center p-4"
            onClick={() => setShowTransferPopup(false)}
          >
            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              className="glass rounded-3xl p-8 max-w-md w-full relative"
              onClick={(e) => e.stopPropagation()}
            >
              <Button
                variant="ghost"
                size="icon"
                className="absolute top-4 right-4"
                onClick={() => setShowTransferPopup(false)}
              >
                <X className="w-4 h-4" />
              </Button>
              <VerificationCodeDisplay 
                code={transferData.verificationCode}
                expiresIn={20}
              />
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Main Content */}
      <main className="relative z-10 container mx-auto px-4 py-12">
        <div className="max-w-4xl mx-auto">
          {/* Hero */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-12"
          >
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Share files <span className="text-gradient">instantly</span>
            </h2>
            <p className="text-lg text-muted-foreground max-w-md mx-auto">
              Upload your files, get a code, share it. It's that simple.
            </p>
          </motion.div>

          {/* Mode Selector or Content */}
          <AnimatePresence mode="wait">
            {!mode && (
              <motion.div
                key="selector"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0, y: -20 }}
              >
                <ModeSelector mode={mode} onModeChange={handleModeChange} />
              </motion.div>
            )}

            {mode === "send" && (
              <motion.div
                key="send"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                className="max-w-xl mx-auto"
              >
                <ModeSelector mode={mode} onModeChange={handleModeChange} />
                
                <div className="glass rounded-3xl p-6 md:p-8">
                  <AnimatePresence mode="wait">
                    {sendStep === "select" && (
                      <motion.div
                        key="select"
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0, x: -20 }}
                        className="space-y-6"
                      >
                        <FileSourceSelector 
                          selectedCloudFiles={selectedCloudFiles}
                          setSelectedCloudFiles={setSelectedCloudFiles}
                          localFiles={localFiles}
                          setLocalFiles={setLocalFiles}
                        />
                        
                        {(selectedCloudFiles.length > 0 || localFiles.length > 0) && (
                          <Button
                            onClick={handleSend}
                            variant="send"
                            size="xl"
                            className="w-full gap-2"
                            disabled={isLoading}
                          >
                            {isLoading ? (
                              <motion.div
                                animate={{ rotate: 360 }}
                                transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                                className="w-5 h-5 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full"
                              />
                            ) : (
                              <>
                                <Send className="w-5 h-5" />
                                Send {selectedCloudFiles.length + localFiles.length} file{(selectedCloudFiles.length + localFiles.length) !== 1 ? 's' : ''}
                              </>
                            )}
                          </Button>
                        )}
                      </motion.div>
                    )}

                    {sendStep === "code" && transferData && (
                      <motion.div
                        key="code"
                        initial={{ opacity: 0, x: 20 }}
                        animate={{ opacity: 1, x: 0 }}
                        className="space-y-6"
                      >
                        <VerificationCodeDisplay 
                          code={transferData.verificationCode}
                          expiresIn={20}
                          transferId={transferData.transferId}
                          onRevoke={resetSend}
                        />
                        
                        <Button
                          onClick={resetSend}
                          variant="outline"
                          size="lg"
                          className="w-full"
                        >
                          Send More Files
                        </Button>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </div>
              </motion.div>
            )}

            {mode === "receive" && (
              <motion.div
                key="receive"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                className="max-w-xl mx-auto"
              >
                <ModeSelector mode={mode} onModeChange={handleModeChange} />
                
                <div className="glass rounded-3xl p-6 md:p-8">
                  <AnimatePresence mode="wait">
                    {!receivedFiles && (
                      <motion.div
                        key="input"
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0, x: -20 }}
                      >
                        <CodeInput
                          onSubmit={handleReceive}
                          isLoading={isLoading}
                          error={receiveError}
                        />
                      </motion.div>
                    )}

                    {receivedFiles && receivedFiles.fileMetaDataList && (
                      <motion.div
                        key="files"
                        initial={{ opacity: 0, x: 20 }}
                        animate={{ opacity: 1, x: 0 }}
                        className="space-y-6"
                      >
                        <FileList
                          files={receivedFiles.fileMetaDataList.map(f => ({
                            id: f.id,
                            originalFileName: f.originalFileName,
                            fileSize: f.fileSize,
                            contentType: f.contentType,
                          }))}
                          showDownloadButton
                          onDownloadAll={handleDownloadAll}
                        />
                        
                        <Button
                          onClick={resetReceive}
                          variant="outline"
                          size="lg"
                          className="w-full"
                        >
                          Receive More Files
                        </Button>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </main>
    </div>
  );
};

const AuthPage = () => {
  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center relative overflow-hidden px-4">
      {/* Background Effects */}
      <div className="fixed inset-0 pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-primary/10 rounded-full blur-3xl animate-pulse-glow" />
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-primary/10 rounded-full blur-3xl animate-pulse-glow" 
             style={{ animationDelay: "1s" }} />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="relative z-10 text-center space-y-8"
      >
        <Logo />
        
        <div className="space-y-2">
          <h2 className="text-3xl md:text-4xl font-bold">
            Welcome to <span className="text-gradient">CloudShare</span>
          </h2>
          <p className="text-muted-foreground max-w-sm mx-auto">
            The fastest way to share files securely. Sign in to get started.
          </p>
        </div>

        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <SignInButton mode="modal">
            <Button variant="send" size="xl">
              Sign In
            </Button>
          </SignInButton>
          <SignUpButton mode="modal">
            <Button variant="receive" size="xl">
              Create Account
            </Button>
          </SignUpButton>
        </div>
      </motion.div>

      {/* Floating elements */}
      <motion.div
        className="absolute top-20 right-20 w-16 h-16 rounded-2xl glass animate-float hidden lg:block"
        style={{ animationDelay: "0s" }}
      />
      <motion.div
        className="absolute bottom-20 left-20 w-12 h-12 rounded-xl glass animate-float hidden lg:block"
        style={{ animationDelay: "0.5s" }}
      />
      <motion.div
        className="absolute top-1/3 right-1/4 w-8 h-8 rounded-lg glass animate-float hidden lg:block"
        style={{ animationDelay: "1s" }}
      />
    </div>
  );
};

const Index = () => {
  const { isSignedIn, isLoaded } = useUser();

  if (!isLoaded) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
          className="w-8 h-8 border-2 border-primary/30 border-t-primary rounded-full"
        />
      </div>
    );
  }

  return isSignedIn ? <Dashboard /> : <AuthPage />;
};

export default Index;
