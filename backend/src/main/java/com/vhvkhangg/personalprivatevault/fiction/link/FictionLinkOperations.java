package com.vhvkhangg.personalprivatevault.fiction.link;

import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.FictionNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.link.command.CreateFictionLinkCommand;
import com.vhvkhangg.personalprivatevault.fiction.link.command.UpdateFictionLinkCommand;
import com.vhvkhangg.personalprivatevault.fiction.link.exception.FictionLinkNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.link.exception.InvalidFictionLinkException;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionLinkView;

import java.util.List;
import java.util.Optional;

/**
 * Public synchronous capability contract for fiction external links.
 */
public interface FictionLinkOperations {

    /**
     * Creates a new link for a fiction work.
     *
     * @param command creation command
     * @return immutable view of the created link
     * @throws FictionNotFoundException if the parent fiction does not exist
     * @throws InvalidFictionLinkException if link attributes or language code are invalid
     */
    FictionLinkView create(CreateFictionLinkCommand command);

    /**
     * Updates an existing link scoped to its parent fiction.
     *
     * @param command update command
     * @return immutable view of the updated link
     * @throws FictionNotFoundException if the parent fiction does not exist
     * @throws FictionLinkNotFoundException if the link does not exist or does not belong to the fiction
     * @throws InvalidFictionLinkException if updated attributes or language code are invalid
     */
    FictionLinkView update(UpdateFictionLinkCommand command);

    /**
     * Retrieves all links for a given fiction work ordered chronologically.
     *
     * @param fictionId ID of the parent fiction
     * @return immutable list of link views
     * @throws FictionNotFoundException if the parent fiction does not exist
     * @throws InvalidFictionLinkException if fictionId is null
     */
    List<FictionLinkView> findByFictionId(Long fictionId);

    /**
     * Finds a specific link by its ID scoped to its parent fiction.
     *
     * @param fictionId ID of the parent fiction
     * @param linkId ID of the link
     * @return optional containing the link view if found and belonging to the fiction, or empty
     */
    Optional<FictionLinkView> findById(Long fictionId, Long linkId);
}
