package com.vhvkhangg.personalprivatevault.journal.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.journal.diary.command.CreateDiaryEntryCommand;
import com.vhvkhangg.personalprivatevault.journal.diary.command.UpdateDiaryEntryCommand;
import com.vhvkhangg.personalprivatevault.journal.internal.web.dto.CreateDiaryEntryRequest;
import com.vhvkhangg.personalprivatevault.journal.internal.web.dto.DiaryEntryResponse;
import com.vhvkhangg.personalprivatevault.journal.internal.web.dto.UpdateDiaryEntryRequest;
import com.vhvkhangg.personalprivatevault.journal.view.DiaryEntryView;

public final class JournalWebMapper {

    private JournalWebMapper() {}

    public static CreateDiaryEntryCommand toCommand(CreateDiaryEntryRequest request) {
        return new CreateDiaryEntryCommand(
                request.entryDate(),
                request.title(),
                request.contentMarkdown()
        );
    }

    public static UpdateDiaryEntryCommand toCommand(Long id, UpdateDiaryEntryRequest request) {
        return new UpdateDiaryEntryCommand(
                id,
                request.entryDate(),
                request.title(),
                request.contentMarkdown()
        );
    }

    public static DiaryEntryResponse toResponse(DiaryEntryView view) {
        return new DiaryEntryResponse(
                view.id(),
                view.entryDate(),
                view.title(),
                view.contentMarkdown(),
                view.createdAt(),
                view.updatedAt(),
                view.deletedAt()
        );
    }
}
