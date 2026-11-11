// @vitest-environment jsdom
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Accounts } from "@/components/drive/accounts";
import type { DriveConnectionDto } from "@/lib/contracts";

describe("Accounts Component", () => {
  const mockConnections: DriveConnectionDto[] = [
    {
      slot: 1,
      status: "CONNECTED",
      displayEmail: "vault.owner@example.com",
      remoteRevocation: null,
    },
    {
      slot: 2,
      status: "DISCONNECTED",
      displayEmail: null,
      remoteRevocation: null,
    },
  ];

  it("renders both slot 1 and slot 2 with appropriate status badges", () => {
    render(
      <Accounts
        connections={mockConnections}
        activeSlot={1}
        onSelectSlot={() => {}}
        onRefreshConnections={() => {}}
      />
    );

    expect(screen.getByText("Tài khoản 1")).toBeInTheDocument();
    expect(screen.getByText("vault.owner@example.com")).toBeInTheDocument();
    expect(screen.getByText("Đã kết nối")).toBeInTheDocument();

    expect(screen.getByText("Tài khoản 2")).toBeInTheDocument();
    expect(screen.getByText("Chưa kết nối")).toBeInTheDocument();
  });

  it("fires onSelectSlot when clicking a slot card", async () => {
    const user = userEvent.setup();
    const selectSlotSpy = vi.fn();

    render(
      <Accounts
        connections={mockConnections}
        activeSlot={1}
        onSelectSlot={selectSlotSpy}
        onRefreshConnections={() => {}}
      />
    );

    const slot2Card = screen.getByRole("button", { name: /Tài khoản 2/ });
    expect(slot2Card).not.toBeNull();
    if (slot2Card) {
      await user.click(slot2Card);
      expect(selectSlotSpy).toHaveBeenCalledWith(2);
    }
  });
});
