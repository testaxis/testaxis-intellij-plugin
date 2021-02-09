<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# TestAxis IntelliJ Plugin Changelog

## [Unreleased]
### Added
- Test code and code under test is now editable from within the plugin
- A new PR builds filter allows to hide PR builds (enabled by default)
- The code under test editor now scrolls to the first line that is covered and changed

### Changed
- Highlighting of code under test is improved to better show the intersection of changed and covered code
- Builds are now sorted by date instead of ID

### Fixed
- An issue where quickly changing tabs would result in a concurrency issue has been fixed

## [0.0.2]
### Fixed
- Websocket connection will no longer be attempted before a valid authentication token is set.
- Remove unused Project Statistics tab.
- Fix initial API calls without initialized settings

## 0.0.1
### Added
- The first release of the TestAxis IntelliJ Plugin.
