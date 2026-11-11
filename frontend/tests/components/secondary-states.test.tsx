// @vitest-environment jsdom
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { StatusPanel } from "@/components/drive/status-panel";
import { ItemGrid } from "@/components/drive/item-grid";
import { FolderDialog } from "@/components/drive/folder-dialog";
import { bffClient } from "@/lib/client/bff-client";

describe("Secondary States", () => {
  it.each(["FOLDER_KEY_EXPIRED", "FOLDER_FAILED"])(
    "resolves protected uncertainty only after canonical definitive %s, then permits explicit new intention",
    async (code) => {
      const user = userEvent.setup();
      const dispatch = vi
        .spyOn(bffClient, "fetchJson")
        .mockRejectedValueOnce(new TypeError("Interrupted before dispatch"))
        .mockRejectedValueOnce(
          Object.assign(new Error("Definitive rejection"), { status: 409, code })
        )
        .mockResolvedValueOnce({ data: {} });
      const created = vi.fn();
      render(<FolderDialog open onOpenChange={vi.fn()} slot={1} onFolderCreated={created} />);
      const input = screen.getByLabelText("Tên thư mục");
      await user.type(input, "Original intention");
      await user.click(screen.getByRole("button", { name: "Tạo thư mục" }));
      expect(input).toHaveAttribute("readonly");
      const original = dispatch.mock.calls[0]![1]!;
      await user.click(screen.getByRole("button", { name: "Tạo thư mục" }));
      expect(input).not.toHaveAttribute("readonly");
      expect(dispatch.mock.calls[1]![1]).toEqual(original);
      expect(dispatch).toHaveBeenCalledTimes(2);
      await user.clear(input);
      await user.type(input, "Explicit new intention");
      expect(dispatch).toHaveBeenCalledTimes(2);
      await user.click(screen.getByRole("button", { name: "Tạo thư mục" }));
      const next = dispatch.mock.calls[2]![1]!;
      expect(next.body).toBe(JSON.stringify({ name: "Explicit new intention" }));
      expect(next.headers).not.toEqual(original.headers);
      expect(created).toHaveBeenCalledOnce();
      dispatch.mockRestore();
    }
  );
  it.each([
    "FOLDER_OUTCOME_UNKNOWN",
    "FOLDER_IN_PROGRESS",
    "FOLDER_KEY_CONFLICT",
    "DRIVE_STALE_AUTHORITY",
  ])("keeps protected uncertainty after nondefinitive 409 %s", async (code) => {
    const user = userEvent.setup();
    const dispatch = vi
      .spyOn(bffClient, "fetchJson")
      .mockRejectedValueOnce(new TypeError("Lost response"))
      .mockRejectedValue(
        Object.assign(new Error("Not a definitive outcome"), { status: 409, code })
      );
    render(<FolderDialog open onOpenChange={vi.fn()} slot={1} onFolderCreated={vi.fn()} />);
    const input = screen.getByLabelText("Tên thư mục");
    await user.type(input, "Protected original");
    await user.click(screen.getByRole("button", { name: "Tạo thư mục" }));
    await user.click(screen.getByRole("button", { name: "Tạo thư mục" }));
    expect(input).toHaveAttribute("readonly");
    await user.type(input, "Replacement");
    expect(input).toHaveValue("Protected original");
    expect(dispatch.mock.calls[1]![1]).toEqual(dispatch.mock.calls[0]![1]);
    dispatch.mockRestore();
  });
  it("allows an explicit different intention with a new key after known rejection", async () => {
    const user = userEvent.setup();
    const dispatch = vi
      .spyOn(bffClient, "fetchJson")
      .mockRejectedValue(
        Object.assign(new Error("Known rejection"), { code: "FOLDER_FAILED", status: 409 })
      );
    render(<FolderDialog open onOpenChange={vi.fn()} slot={1} onFolderCreated={vi.fn()} />);
    await user.type(screen.getByLabelText("Tên thư mục"), "Rejected");
    await user.click(screen.getByRole("button", { name: "Tạo thư mục" }));
    await screen.findByRole("alert");
    const first = dispatch.mock.calls[0]![1]!;
    await user.clear(screen.getByLabelText("Tên thư mục"));
    await user.type(screen.getByLabelText("Tên thư mục"), "New intention");
    await user.click(screen.getByRole("button", { name: "Tạo thư mục" }));
    const next = dispatch.mock.calls[1]![1]!;
    expect(next.body).not.toBe(first.body);
    expect(next.headers).not.toEqual(first.headers);
    dispatch.mockRestore();
  });
  it("retains the original folder name and key after a lost reply and close/reopen", async () => {
    const user = userEvent.setup();
    const dispatch = vi
      .spyOn(bffClient, "fetchJson")
      .mockRejectedValue(new TypeError("Network response lost"));
    const props = { onOpenChange: vi.fn(), slot: 1 as const, onFolderCreated: vi.fn() };
    const { rerender } = render(<FolderDialog open {...props} />);
    await user.type(screen.getByLabelText("Tên thư mục"), "Original intention");
    await user.click(screen.getByRole("button", { name: "Tạo thư mục" }));
    expect(await screen.findByRole("alert")).toBeVisible();
    const first = dispatch.mock.calls[0]![1]!;
    rerender(<FolderDialog open={false} {...props} />);
    rerender(<FolderDialog open {...props} />);
    expect(screen.getByLabelText("Tên thư mục")).toHaveValue("Original intention");
    expect(screen.getByLabelText("Tên thư mục")).toHaveAttribute("readonly");
    await user.type(screen.getByLabelText("Tên thư mục"), "Replacement");
    expect(screen.getByLabelText("Tên thư mục")).toHaveValue("Original intention");
    await user.click(screen.getByRole("button", { name: "Tạo thư mục" }));
    const retry = dispatch.mock.calls[1]![1]!;
    expect(retry.body).toBe(first.body);
    expect(retry.headers).toEqual(first.headers);
    dispatch.mockRestore();
  });
  it("renders ROOT_NOT_READY panel and triggers action", async () => {
    const user = userEvent.setup();
    const actionSpy = vi.fn();

    render(<StatusPanel type="ROOT_NOT_READY" slot={1} onAction={actionSpy} />);

    expect(
      screen.getByText("Thư mục gốc PPV chưa được khởi tạo cho tài khoản này.")
    ).toBeInTheDocument();
    const ensureBtn = screen.getByText("Khởi tạo thư mục PPV");
    await user.click(ensureBtn);
    expect(actionSpy).toHaveBeenCalled();
  });

  it("renders RECONNECT_REQUIRED panel", () => {
    render(<StatusPanel type="RECONNECT_REQUIRED" slot={1} onAction={() => {}} />);

    expect(screen.getByText("Cần kết nối lại")).toBeInTheDocument();
  });

  it("renders DISCONNECTED panel", () => {
    render(<StatusPanel type="DISCONNECTED" slot={1} onAction={() => {}} />);

    expect(screen.getByText("Chưa kết nối")).toBeInTheDocument();
  });

  it("renders empty root message in ItemGrid when items list is empty", () => {
    render(
      <ItemGrid
        items={[]}
        slot={1}
        loading={false}
        loadingMore={false}
        hasMore={false}
        onLoadMore={() => {}}
        onOpenFolder={() => {}}
        isSearchActive={false}
      />
    );

    expect(
      screen.getByText("Chưa có tệp hoặc thư mục nào trong thư mục PPV này.")
    ).toBeInTheDocument();
  });

  it("renders empty search message in ItemGrid when search is active", () => {
    render(
      <ItemGrid
        items={[]}
        slot={1}
        loading={false}
        loadingMore={false}
        hasMore={false}
        onLoadMore={() => {}}
        onOpenFolder={() => {}}
        isSearchActive={true}
      />
    );

    expect(
      screen.getByText("Không tìm thấy tệp hoặc thư mục phù hợp với tiêu chí tìm kiếm.")
    ).toBeInTheDocument();
  });
});
