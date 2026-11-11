// @vitest-environment jsdom
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { UploadPanel } from "@/components/drive/upload-panel";
import { FolderDialog } from "@/components/drive/folder-dialog";
import type { PendingUploadAction } from "@/lib/client/upload-action";

describe("Upload and Folder Actions", () => {
  it("renders upload action in uncertain state with retry button", async () => {
    const mockFile = new File(["test-content"], "document.pdf", { type: "application/pdf" });
    const action: PendingUploadAction = {
      idempotencyKey: "20261109T000000Z.11111111-2222-3333-4444-555555555555",
      file: mockFile,
      slot: 1,
      folderId: null,
      progressPercent: 50,
      status: "UNCERTAIN",
      errorMessage: "Mất kết nối mạng trong khi tải tệp.",
    };

    render(
      <UploadPanel actions={[action]} onActionUpdated={() => {}} onDismissAction={() => {}} />
    );

    expect(screen.getByText("document.pdf")).toBeInTheDocument();
    expect(screen.getByText("Mất kết nối mạng trong khi tải tệp.")).toBeInTheDocument();
    expect(screen.getByText("Thử lại cùng hành động")).toBeInTheDocument();
  });

  it("validates folder name in FolderDialog before submission", async () => {
    const user = userEvent.setup();
    render(
      <FolderDialog open={true} onOpenChange={() => {}} slot={1} onFolderCreated={() => {}} />
    );

    const input = screen.getByPlaceholderText("Nhập tên thư mục...");
    const createBtn = screen.getByText("Tạo thư mục");

    // Invalid slash name
    await user.type(input, "folder/slash");
    await user.click(createBtn);

    expect(
      await screen.findByText(/Tên thư mục không được chứa dấu gạch chéo/)
    ).toBeInTheDocument();
  });
});
