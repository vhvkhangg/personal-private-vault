"use client";

import { generateActionKey } from "../format";
import { bffClient } from "./bff-client";
import type { DriveFileDto } from "../contracts";

let activeUploads = 0;

export type UploadStateStatus =
  "IDLE" | "UPLOADING" | "SUCCESS" | "RECONCILED" | "IN_PROGRESS" | "UNCERTAIN" | "ERROR";

export interface PendingUploadAction {
  idempotencyKey: string;
  file: File;
  slot: 1 | 2;
  folderId: string | null;
  progressPercent: number;
  status: UploadStateStatus;
  errorMessage?: string;
  resultFile?: DriveFileDto;
  cancel?: () => void;
}

export function createUploadAction(
  file: File,
  slot: 1 | 2,
  folderId: string | null
): PendingUploadAction {
  return {
    idempotencyKey: generateActionKey(),
    file,
    slot,
    folderId,
    progressPercent: 0,
    status: "IDLE",
  };
}

export async function executeUpload(
  action: PendingUploadAction,
  onProgress?: (percent: number) => void
): Promise<DriveFileDto> {
  if (action.status === "UPLOADING") throw new Error("Hành động đang được tải lên.");
  if (activeUploads >= 2) {
    action.status = "ERROR";
    action.errorMessage = "Tối đa hai lượt tải lên đồng thời.";
    throw new Error(action.errorMessage);
  }
  const maxFileBytes = 52_428_800; // 50 MiB
  if (action.file.size > maxFileBytes) {
    action.status = "ERROR";
    action.errorMessage = "Tệp vượt quá giới hạn tối đa 50 MiB.";
    throw new Error(action.errorMessage);
  }

  const csrfToken = bffClient.getCsrfToken();
  const url = action.folderId
    ? `/api/bff/drive/connections/${action.slot}/folders/${action.folderId}/files`
    : `/api/bff/drive/connections/${action.slot}/files`;

  action.status = "UPLOADING";
  activeUploads++;

  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    let released = false;
    const release = () => {
      if (!released) {
        released = true;
        activeUploads--;
        delete action.cancel;
      }
    };
    action.cancel = () => xhr.abort();
    xhr.timeout = 180_000;
    xhr.open("POST", url);

    if (csrfToken) {
      xhr.setRequestHeader("X-PPV-CSRF", csrfToken);
    }
    xhr.setRequestHeader("Idempotency-Key", action.idempotencyKey);

    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) {
        const percent = Math.min(100, Math.round((e.loaded / e.total) * 100));
        action.progressPercent = percent;
        onProgress?.(percent);
      }
    };

    xhr.onload = () => {
      release();
      if (xhr.status === 201 || xhr.status === 200) {
        try {
          const json = JSON.parse(xhr.responseText);
          const fileDto: DriveFileDto = json.data;
          action.status = xhr.status === 201 ? "SUCCESS" : "RECONCILED";
          action.resultFile = fileDto;
          resolve(fileDto);
        } catch {
          action.status = "UNCERTAIN";
          action.errorMessage = "Không thể phân tích phản hồi từ máy chủ.";
          reject(new Error(action.errorMessage));
        }
      } else {
        let code = `HTTP_${xhr.status}`;
        let message = "Tải lên thất bại.";
        try {
          const json = JSON.parse(xhr.responseText);
          code = json.error?.code || code;
          message = json.error?.message || message;
        } catch {
          // fallback
        }

        if (xhr.status === 409) {
          if (code === "UPLOAD_IN_PROGRESS") {
            action.status = "IN_PROGRESS";
            action.errorMessage = "Thao tác tải lên đang được xử lý...";
          } else if (code === "UPLOAD_KEY_CONFLICT") {
            action.status = "ERROR";
            action.errorMessage = "Mã hành động bị xung đột dữ liệu.";
          } else {
            action.status = "UNCERTAIN";
            action.errorMessage = message || "Trạng thái tải lên không chắc chắn.";
          }
        } else if (xhr.status >= 500) {
          action.status = "UNCERTAIN";
          action.errorMessage = message;
        } else {
          action.status = "ERROR";
          action.errorMessage = message;
        }
        reject(new Error(action.errorMessage));
      }
    };

    xhr.onerror = () => {
      release();
      // Network interruption -> outcome uncertain
      action.status = "UNCERTAIN";
      action.errorMessage = "Mất kết nối mạng trong khi tải tệp. Vui lòng thử lại cùng hành động.";
      reject(new Error(action.errorMessage));
    };

    xhr.onabort = () => {
      release();
      action.status = "UNCERTAIN";
      action.errorMessage = "Quá trình tải lên đã bị hủy.";
      reject(new Error(action.errorMessage));
    };
    xhr.ontimeout = () => {
      release();
      action.status = "UNCERTAIN";
      action.errorMessage = "Hết thời gian chờ. Thử lại cùng hành động để kiểm tra kết quả.";
      reject(new Error(action.errorMessage));
    };

    const formData = new FormData();
    formData.append("file", action.file, action.file.name);
    try {
      xhr.send(formData);
    } catch (error) {
      release();
      reject(error);
    }
  });
}
