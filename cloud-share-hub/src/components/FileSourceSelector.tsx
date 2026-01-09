import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { FolderOpen, HardDrive, Check, FileIcon, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { cn } from "@/lib/utils";
import { api, FileEntityDTO } from "@/lib/api";

type FileSource = "computer" | "cloud" | null;

interface FileSourceSelectorProps {
  selectedCloudFiles: FileEntityDTO[];
  setSelectedCloudFiles: React.Dispatch<React.SetStateAction<FileEntityDTO[]>>;
  localFiles: File[];
  setLocalFiles: React.Dispatch<React.SetStateAction<File[]>>;
}

const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

export const FileSourceSelector = ({
  selectedCloudFiles,
  setSelectedCloudFiles,
  localFiles,
  setLocalFiles,
}: FileSourceSelectorProps) => {
  const [source, setSource] = useState<FileSource>(null);
  const [cloudFiles, setCloudFiles] = useState<FileEntityDTO[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => {
    if (source === "cloud") {
      loadCloudFiles();
    }
  }, [source]);

  const loadCloudFiles = async () => {
    setIsLoading(true);
    try {
      const files = await api.getAllFiles();
      setCloudFiles(files);
    } catch (error) {
      console.error("Failed to load cloud files:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const toggleCloudFile = (file: FileEntityDTO) => {
    setSelectedCloudFiles(prev => {
      const exists = prev.some(f => f.fileId === file.fileId);
      if (exists) {
        return prev.filter(f => f.fileId !== file.fileId);
      }
      return [...prev, file];
    });
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    const droppedFiles = Array.from(e.dataTransfer.files);
    setLocalFiles(prev => [...prev, ...droppedFiles]);
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const selectedFiles = Array.from(e.target.files);
      setLocalFiles(prev => [...prev, ...selectedFiles]);
    }
  };

  const removeLocalFile = (index: number) => {
    setLocalFiles(prev => prev.filter((_, i) => i !== index));
  };

  const totalSelected = selectedCloudFiles.length + localFiles.length;

  return (
    <div className="space-y-4">
      {/* Source Selection */}
      <div className="grid grid-cols-2 gap-3">
        <motion.button
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          onClick={() => setSource(source === "cloud" ? null : "cloud")}
          className={cn(
            "relative p-4 rounded-xl border-2 transition-all duration-200 text-left",
            source === "cloud"
              ? "border-primary bg-primary/10"
              : "border-border hover:border-primary/50 hover:bg-secondary/30"
          )}
        >
          {source === "cloud" && (
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              className="absolute top-2 right-2 w-5 h-5 rounded-full bg-primary flex items-center justify-center"
            >
              <Check className="w-3 h-3 text-primary-foreground" />
            </motion.div>
          )}
          <FolderOpen className={cn(
            "w-8 h-8 mb-2",
            source === "cloud" ? "text-primary" : "text-muted-foreground"
          )} />
          <p className="font-medium">My Files</p>
          <p className="text-xs text-muted-foreground">Choose from uploaded</p>
        </motion.button>

        <motion.button
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          onClick={() => setSource(source === "computer" ? null : "computer")}
          className={cn(
            "relative p-4 rounded-xl border-2 transition-all duration-200 text-left",
            source === "computer"
              ? "border-primary bg-primary/10"
              : "border-border hover:border-primary/50 hover:bg-secondary/30"
          )}
        >
          {source === "computer" && (
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              className="absolute top-2 right-2 w-5 h-5 rounded-full bg-primary flex items-center justify-center"
            >
              <Check className="w-3 h-3 text-primary-foreground" />
            </motion.div>
          )}
          <HardDrive className={cn(
            "w-8 h-8 mb-2",
            source === "computer" ? "text-primary" : "text-muted-foreground"
          )} />
          <p className="font-medium">Computer</p>
          <p className="text-xs text-muted-foreground">Browse local files</p>
        </motion.button>
      </div>

      {/* Cloud Files Panel */}
      <AnimatePresence>
        {source === "cloud" && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="overflow-hidden"
          >
            <div className="border border-border rounded-xl max-h-64 overflow-y-auto">
              {isLoading ? (
                <div className="p-8 flex items-center justify-center">
                  <Loader2 className="w-6 h-6 animate-spin text-primary" />
                </div>
              ) : cloudFiles.length === 0 ? (
                <div className="p-8 text-center text-muted-foreground">
                  <FolderOpen className="w-8 h-8 mx-auto mb-2 opacity-50" />
                  <p>No files uploaded yet</p>
                </div>
              ) : (
                <div className="divide-y divide-border/50">
                  {cloudFiles.map(file => {
                    const isSelected = selectedCloudFiles.some(f => f.fileId === file.fileId);
                    return (
                      <motion.div
                        key={file.fileId}
                        whileHover={{ backgroundColor: "hsl(var(--muted) / 0.3)" }}
                        className={cn(
                          "flex items-center gap-3 p-3 cursor-pointer transition-colors",
                          isSelected && "bg-primary/5"
                        )}
                        onClick={() => toggleCloudFile(file)}
                      >
                        <Checkbox checked={isSelected} />
                        <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                          <FileIcon className="w-4 h-4 text-primary" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium truncate">{file.originalFileName}</p>
                          <p className="text-xs text-muted-foreground">{formatFileSize(file.fileSize)}</p>
                        </div>
                      </motion.div>
                    );
                  })}
                </div>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Local Files Panel */}
      <AnimatePresence>
        {source === "computer" && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="overflow-hidden"
          >
            <div
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
              className={cn(
                "relative border-2 border-dashed rounded-xl p-6 transition-all duration-200 cursor-pointer",
                isDragging
                  ? "border-primary bg-primary/5"
                  : "border-border hover:border-primary/50"
              )}
            >
              <input
                type="file"
                multiple
                onChange={handleFileSelect}
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
              />
              <div className="text-center">
                <HardDrive className="w-10 h-10 mx-auto mb-2 text-muted-foreground" />
                <p className="font-medium">
                  {isDragging ? "Drop files here" : "Drag & drop or click to browse"}
                </p>
              </div>
            </div>

            {/* Selected Local Files */}
            {localFiles.length > 0 && (
              <div className="mt-3 border border-border rounded-xl max-h-48 overflow-y-auto divide-y divide-border/50">
                {localFiles.map((file, index) => (
                  <motion.div
                    key={`${file.name}-${index}`}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    className="flex items-center gap-3 p-3"
                  >
                    <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                      <FileIcon className="w-4 h-4 text-primary" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium truncate">{file.name}</p>
                      <p className="text-xs text-muted-foreground">{formatFileSize(file.size)}</p>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => removeLocalFile(index)}
                      className="text-destructive hover:text-destructive"
                    >
                      Remove
                    </Button>
                  </motion.div>
                ))}
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      {/* Selection Summary */}
      {totalSelected > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-center text-sm text-muted-foreground"
        >
          {selectedCloudFiles.length > 0 && (
            <span>{selectedCloudFiles.length} cloud file(s)</span>
          )}
          {selectedCloudFiles.length > 0 && localFiles.length > 0 && <span> + </span>}
          {localFiles.length > 0 && (
            <span>{localFiles.length} local file(s)</span>
          )}
          <span className="text-primary font-medium"> = {totalSelected} total</span>
        </motion.div>
      )}
    </div>
  );
};
