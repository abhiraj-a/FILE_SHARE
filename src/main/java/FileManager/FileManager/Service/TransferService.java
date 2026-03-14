package FileManager.FileManager.Service;


import FileManager.FileManager.DTO.FileTransferDTO;
import FileManager.FileManager.Utils.ClerkUserPrincipal;

import java.util.List;
import java.util.UUID;

public interface TransferService  {

 FileTransferDTO transferFiles(ClerkUserPrincipal principal  , List<UUID> fileIds);

 FileTransferDTO recieveByCode(ClerkUserPrincipal principal , String verificationCode);

 void revokeTransfer(ClerkUserPrincipal principal , UUID fileTransferId);

 FileTransferDTO downloadTransfer(ClerkUserPrincipal principal, String verificationCode);
}
