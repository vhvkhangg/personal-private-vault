// @vitest-environment jsdom
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Toolbar } from "@/components/drive/toolbar";

describe("Toolbar Filters", () => {
  it("triggers filter callbacks when inputs change", async () => {
    const user = userEvent.setup();
    const searchChangeSpy = vi.fn();
    const fromDateChangeSpy = vi.fn();
    const toDateChangeSpy = vi.fn();
    const resetSpy = vi.fn();

    const { rerender } = render(
      <Toolbar
        searchQuery=""
        onSearchChange={searchChangeSpy}
        typeFilter="all"
        onTypeFilterChange={() => {}}
        fromDate=""
        onFromDateChange={fromDateChangeSpy}
        toDate=""
        onToDateChange={toDateChangeSpy}
        onResetFilters={resetSpy}
        onOpenFileUpload={() => {}}
        onOpenNewFolder={() => {}}
        isInsideChildFolder={false}
      />
    );

    const searchInput = screen.getByPlaceholderText("Tìm trong Drive hiện tại...");
    await user.type(searchInput, "baocao");
    expect(searchChangeSpy).toHaveBeenCalled();

    // Rerender with active filter to show reset button
    rerender(
      <Toolbar
        searchQuery="baocao"
        onSearchChange={searchChangeSpy}
        typeFilter="all"
        onTypeFilterChange={() => {}}
        fromDate=""
        onFromDateChange={fromDateChangeSpy}
        toDate=""
        onToDateChange={toDateChangeSpy}
        onResetFilters={resetSpy}
        onOpenFileUpload={() => {}}
        onOpenNewFolder={() => {}}
        isInsideChildFolder={false}
      />
    );

    const resetBtn = screen.getByTitle("Đặt lại bộ lọc");
    expect(resetBtn).toBeInTheDocument();
    await user.click(resetBtn);
    expect(resetSpy).toHaveBeenCalled();
  });
});
