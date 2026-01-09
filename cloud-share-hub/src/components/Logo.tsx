import { motion } from "framer-motion";
import { Cloud } from "lucide-react";

export const Logo = () => {
  return (
    <motion.div 
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      className="flex items-center gap-3"
    >
      <div className="relative">
        <div className="absolute inset-0 gradient-primary blur-xl opacity-50" />
        <div className="relative w-10 h-10 rounded-xl gradient-primary flex items-center justify-center shadow-button">
          <Cloud className="w-6 h-6 text-primary-foreground" />
        </div>
      </div>
      <div>
        <h1 className="text-xl font-bold text-foreground">CloudShare</h1>
        <p className="text-xs text-muted-foreground">Fast. Secure. Simple.</p>
      </div>
    </motion.div>
  );
};
