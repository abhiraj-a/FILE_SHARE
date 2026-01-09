import { motion } from "framer-motion";
import { useState, useRef } from "react";
import { Button } from "@/components/ui/button";
import { Download, FileIcon, AlertCircle } from "lucide-react";

interface CodeInputProps {
  onSubmit: (code: string) => void;
  isLoading?: boolean;
  error?: string | null;
}

export const CodeInput = ({ onSubmit, isLoading, error }: CodeInputProps) => {
  const [code, setCode] = useState(["", "", "", "", ""]);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  const handleChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return;
    
    const newCode = [...code];
    newCode[index] = value.slice(-1);
    setCode(newCode);

    if (value && index < 4) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === "Backspace" && !code[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
    if (e.key === "Enter" && code.every(d => d)) {
      onSubmit(code.join(""));
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 5);
    const newCode = [...code];
    pasted.split("").forEach((char, i) => {
      if (i < 5) newCode[i] = char;
    });
    setCode(newCode);
    if (pasted.length === 5) {
      inputRefs.current[4]?.focus();
    }
  };

  const isComplete = code.every(d => d);

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="flex flex-col items-center space-y-6"
    >
      <div className="text-center">
        <h3 className="text-xl font-semibold text-foreground mb-2">Enter the code</h3>
        <p className="text-muted-foreground text-sm">
          Enter the 5-digit code shared by the sender
        </p>
      </div>

      {/* Code Input */}
      <div className="flex gap-2 md:gap-3">
        {code.map((digit, index) => (
          <motion.input
            key={index}
            ref={el => inputRefs.current[index] = el}
            type="text"
            inputMode="numeric"
            value={digit}
            onChange={e => handleChange(index, e.target.value)}
            onKeyDown={e => handleKeyDown(index, e)}
            onPaste={index === 0 ? handlePaste : undefined}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.05 }}
            className="w-12 h-16 md:w-16 md:h-20 rounded-xl glass text-center font-mono text-2xl md:text-4xl font-bold text-foreground 
                       focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent
                       transition-all duration-200"
            maxLength={1}
            disabled={isLoading}
          />
        ))}
      </div>

      {/* Error Message */}
      {error && (
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex items-center gap-2 text-destructive text-sm"
        >
          <AlertCircle className="w-4 h-4" />
          {error}
        </motion.div>
      )}

      {/* Submit Button */}
      <Button
        onClick={() => onSubmit(code.join(""))}
        variant="send"
        size="xl"
        disabled={!isComplete || isLoading}
        className="w-full max-w-xs gap-2"
      >
        {isLoading ? (
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
            className="w-5 h-5 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full"
          />
        ) : (
          <>
            <Download className="w-5 h-5" />
            Receive Files
          </>
        )}
      </Button>
    </motion.div>
  );
};
