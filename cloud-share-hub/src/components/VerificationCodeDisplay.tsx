import { motion } from "framer-motion";
import { Copy, Check, XCircle } from "lucide-react";
import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { api } from "@/lib/api";
import { toast } from "sonner";

interface VerificationCodeDisplayProps {
  code: string;
  expiresIn?: number;
  transferId?: string;
  onRevoke?: () => void;
}

export const VerificationCodeDisplay = ({ 
  code, 
  expiresIn = 20, 
  transferId,
  onRevoke 
}: VerificationCodeDisplayProps) => {
  const [copied, setCopied] = useState(false);
  const [isRevoking, setIsRevoking] = useState(false);
  const [timeLeft, setTimeLeft] = useState(expiresIn * 60);

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(prev => Math.max(0, prev - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const copyCode = async () => {
    await navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleRevoke = async () => {
    if (!transferId) return;
    
    setIsRevoking(true);
    try {
      await api.revokeTransfer(transferId);
      toast.success("Transfer revoked successfully");
      onRevoke?.();
    } catch (error) {
      toast.error("Failed to revoke transfer");
    } finally {
      setIsRevoking(false);
    }
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const digits = code.split("");

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.9 }}
      animate={{ opacity: 1, scale: 1 }}
      className="flex flex-col items-center space-y-6"
    >
      <div className="text-center">
        <h3 className="text-xl font-semibold text-foreground mb-2">Share this code</h3>
        <p className="text-muted-foreground text-sm">
          The recipient can use this code to download your files
        </p>
      </div>

      {/* Code Display */}
      <div className="relative">
        <div className="absolute inset-0 gradient-primary blur-3xl opacity-20 animate-pulse-glow" />
        <div className="relative flex gap-2 md:gap-3">
          {digits.map((digit, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20, rotateX: -90 }}
              animate={{ opacity: 1, y: 0, rotateX: 0 }}
              transition={{ 
                delay: index * 0.1,
                type: "spring",
                stiffness: 200 
              }}
              className="w-12 h-16 md:w-16 md:h-20 rounded-xl glass flex items-center justify-center"
            >
              <span className="font-mono text-2xl md:text-4xl font-bold text-gradient">
                {digit}
              </span>
            </motion.div>
          ))}
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-3">
        <Button
          onClick={copyCode}
          variant="glass"
          size="lg"
          className="gap-2"
        >
          {copied ? (
            <>
              <Check className="w-5 h-5 text-success" />
              Copied!
            </>
          ) : (
            <>
              <Copy className="w-5 h-5" />
              Copy Code
            </>
          )}
        </Button>

        {transferId && (
          <Button
            onClick={handleRevoke}
            variant="outline"
            size="lg"
            className="gap-2 border-destructive/30 text-destructive hover:bg-destructive/10"
            disabled={isRevoking}
          >
            <XCircle className="w-5 h-5" />
            {isRevoking ? "Revoking..." : "Revoke"}
          </Button>
        )}
      </div>

      {/* Timer */}
      <motion.div 
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5 }}
        className="flex items-center gap-2 text-muted-foreground text-sm"
      >
        <div className={`w-2 h-2 rounded-full ${timeLeft < 60 ? 'bg-destructive' : 'bg-primary'} animate-pulse`} />
        <span>Expires in {formatTime(timeLeft)}</span>
      </motion.div>
    </motion.div>
  );
};
