import { motion, AnimatePresence } from "framer-motion";
import { FileIcon, Download, Check, X } from "lucide-react";
import { Button } from "@/components/ui/button";

interface FileItem {
  id: string;
  originalFileName: string;
  fileSize: number;
  contentType?: string;
  downloadUrl?: string;
}

interface FileListProps {
  files: FileItem[];
  onDownload?: (file: FileItem) => void;
  onDownloadAll?: () => void;
  selectable?: boolean;
  selectedIds?: string[];
  onSelectionChange?: (ids: string[]) => void;
  showDownloadButton?: boolean;
}

export const FileList = ({ 
  files, 
  onDownload, 
  onDownloadAll,
  selectable,
  selectedIds = [],
  onSelectionChange,
  showDownloadButton = false
}: FileListProps) => {
  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const getFileIcon = (contentType?: string) => {
    return FileIcon;
  };

  const toggleSelection = (id: string) => {
    if (!onSelectionChange) return;
    if (selectedIds.includes(id)) {
      onSelectionChange(selectedIds.filter(i => i !== id));
    } else {
      onSelectionChange([...selectedIds, id]);
    }
  };

  const totalSize = files.reduce((acc, f) => acc + f.fileSize, 0);

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="space-y-4"
    >
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <p className="text-lg font-medium">{files.length} file{files.length !== 1 ? 's' : ''}</p>
          <p className="text-sm text-muted-foreground">{formatFileSize(totalSize)} total</p>
        </div>
        {showDownloadButton && onDownloadAll && (
          <Button onClick={onDownloadAll} variant="send" size="lg" className="gap-2">
            <Download className="w-5 h-5" />
            Download All
          </Button>
        )}
      </div>

      {/* File Grid */}
      <div className="grid gap-3">
        <AnimatePresence>
          {files.map((file, index) => {
            const Icon = getFileIcon(file.contentType);
            const isSelected = selectedIds.includes(file.id);

            return (
              <motion.div
                key={file.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ delay: index * 0.05 }}
                onClick={() => selectable && toggleSelection(file.id)}
                className={`
                  flex items-center gap-4 p-4 rounded-xl transition-all duration-200 group
                  ${selectable ? 'cursor-pointer' : ''}
                  ${isSelected 
                    ? 'bg-primary/10 border border-primary' 
                    : 'bg-secondary/50 border border-transparent hover:bg-secondary'}
                `}
              >
                {/* Selection Indicator */}
                {selectable && (
                  <div className={`
                    w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all
                    ${isSelected 
                      ? 'bg-primary border-primary' 
                      : 'border-muted-foreground/30 group-hover:border-primary/50'}
                  `}>
                    {isSelected && <Check className="w-4 h-4 text-primary-foreground" />}
                  </div>
                )}

                {/* File Icon */}
                <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center flex-shrink-0">
                  <Icon className="w-6 h-6 text-primary" />
                </div>

                {/* File Info */}
                <div className="flex-1 min-w-0">
                  <p className="font-medium truncate">{file.originalFileName}</p>
                  <p className="text-sm text-muted-foreground">{formatFileSize(file.fileSize)}</p>
                </div>

                {/* Download Button */}
                {onDownload && !selectable && (
                  <Button
                    onClick={() => onDownload(file)}
                    variant="ghost"
                    size="icon"
                    className="opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <Download className="w-5 h-5" />
                  </Button>
                )}
              </motion.div>
            );
          })}
        </AnimatePresence>
      </div>
    </motion.div>
  );
};
