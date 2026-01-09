import { motion } from "framer-motion";
import { Send, Download, ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";

type Mode = "send" | "receive";

interface ModeSelectorProps {
  mode: Mode | null;
  onModeChange: (mode: Mode | null) => void;
}

export const ModeSelector = ({ mode, onModeChange }: ModeSelectorProps) => {
  if (mode) {
    return (
      <motion.button
        initial={{ opacity: 0, x: -20 }}
        animate={{ opacity: 1, x: 0 }}
        onClick={() => onModeChange(null)}
        className="flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-6"
      >
        <ArrowLeft className="w-4 h-4" />
        Back
      </motion.button>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="grid md:grid-cols-2 gap-6 w-full max-w-2xl mx-auto"
    >
      {/* Send Card */}
      <motion.button
        onClick={() => onModeChange("send")}
        whileHover={{ scale: 1.03, y: -5 }}
        whileTap={{ scale: 0.98 }}
        className="group relative p-8 rounded-3xl glass overflow-hidden transition-all duration-300 
                   hover:border-primary/50 text-left"
      >
        <div className="absolute inset-0 gradient-primary opacity-0 group-hover:opacity-10 transition-opacity" />
        <div className="absolute top-0 right-0 w-32 h-32 gradient-primary opacity-10 blur-3xl 
                        group-hover:opacity-30 transition-opacity" />
        
        <div className="relative z-10 space-y-4">
          <div className="w-16 h-16 rounded-2xl gradient-primary flex items-center justify-center
                          shadow-button group-hover:shadow-glow transition-all">
            <Send className="w-8 h-8 text-primary-foreground" />
          </div>
          <div>
            <h3 className="text-2xl font-bold text-foreground mb-2">Send</h3>
            <p className="text-muted-foreground">
              Upload files and share a code with the recipient
            </p>
          </div>
        </div>
      </motion.button>

      {/* Receive Card */}
      <motion.button
        onClick={() => onModeChange("receive")}
        whileHover={{ scale: 1.03, y: -5 }}
        whileTap={{ scale: 0.98 }}
        className="group relative p-8 rounded-3xl glass overflow-hidden transition-all duration-300
                   hover:border-primary/50 text-left"
      >
        <div className="absolute inset-0 bg-secondary opacity-0 group-hover:opacity-50 transition-opacity" />
        <div className="absolute top-0 right-0 w-32 h-32 bg-primary/20 opacity-10 blur-3xl 
                        group-hover:opacity-30 transition-opacity" />
        
        <div className="relative z-10 space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-secondary border border-primary/30 
                          flex items-center justify-center group-hover:border-primary transition-all">
            <Download className="w-8 h-8 text-primary" />
          </div>
          <div>
            <h3 className="text-2xl font-bold text-foreground mb-2">Receive</h3>
            <p className="text-muted-foreground">
              Enter a code to download shared files
            </p>
          </div>
        </div>
      </motion.button>
    </motion.div>
  );
};
