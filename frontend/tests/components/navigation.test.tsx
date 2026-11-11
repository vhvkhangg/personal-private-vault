// @vitest-environment jsdom
import { describe, it, expect, vi } from "vitest";
import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { PrivateShell } from "@/components/layout/private-shell";
import { bffClient } from "@/lib/client/bff-client";

describe("PrivateShell Navigation", () => {
  it("renders skip-link, primary rail, contextual sidebar, and content", () => {
    render(
      <PrivateShell>
        <div data-testid="test-content">Explorer Content</div>
      </PrivateShell>
    );

    // Skip to main content link for a11y
    const skipLink = screen.getByText("Chuyển đến nội dung chính");
    expect(skipLink).toBeInTheDocument();
    expect(skipLink).toHaveAttribute("href", "#main-content");

    // Home is disabled placeholder
    const homeBtn = screen.getByLabelText(/Trang chủ/);
    expect(homeBtn).toBeDisabled();

    // Personal is active
    const personalBtn = screen.getByRole("button", { name: "Cá nhân" });
    expect(personalBtn).toHaveAttribute("aria-current", "page");

    // Contextual sidebar has Google Drive
    expect(screen.getByText("Google Drive")).toBeInTheDocument();
    expect(screen.getByTestId("test-content")).toBeInTheDocument();
  });

  it("triggers bffClient.logout when logout button is clicked", async () => {
    const user = userEvent.setup();
    const logoutSpy = vi.spyOn(bffClient, "logout").mockResolvedValue(undefined);

    render(
      <PrivateShell>
        <div>Content</div>
      </PrivateShell>
    );

    const logoutBtn = within(
      screen.getByRole("navigation", { name: "Điều hướng chính" })
    ).getByLabelText("Đăng xuất");
    await user.click(logoutBtn);

    expect(logoutSpy).toHaveBeenCalled();
  });
});
