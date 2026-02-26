<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# File Closer Changelog

## [Unreleased]

## [0.0.1]
### Added
- Extension-grouped tree view in the right sidebar tool window
- Bulk close: click the trash icon on an extension group to close all files of that type
- Single close: expand a group and click the trash icon on an individual file
- Unsaved-file safety dialog (save / discard / cancel) before closing modified files
- Live tree updates when files are opened or closed
- Duplicate file name detection: files with the same name show their full path

### Fixed
- Click on the text area of an extension entry no longer closes files (only the trash icon triggers close)
- Hit-test now uses actual component layout instead of pixel-coordinate calculation
