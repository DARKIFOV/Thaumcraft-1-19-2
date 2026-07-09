# Stage603-622 research table recipe cleanup

Original TC4 research notes are produced through the Research Table workflow, not by a normal crafting-table JSON recipe.
This stage removes the old duplicate `research_note.json` and `research_note_original_style.json` recipe files so the active path is:

1. slot 0: Scribing Tools with ink;
2. slot 1: empty Research Note slot;
3. player inventory has paper;
4. right-click the original note-slot hotzone;
5. server creates the note, consumes paper and 1 scribing ink, and writes the original research metadata to NBT.

This is a parity cleanup, not a new mechanic.
