# ANIMA: Of chanting beasts

Simple GUI game intended to be played one Vs. one, on a single board.
Each player has a team composed of three unit types: wolf, hare, and tiger.
Players can move their units along a grid; attack other units; and call for reinforcements.

The game is still in development, and it can hardly be considered as finished.
Nonetheless, working on this project taught me so much (not only on software development), that I still decided to submit this Beta.

## (Updated) Requirements

- Java JDK version 21 or higher
- Gradle version 9.1 or higher

## Usage

### From a terminal

If the Gradle wrapper is broken (e.g., missing the wrapper .jar), or if you want to update the Gradle version, first install Gradle, then run:

```bash
gradle wrapper
```
#### Starting the application

- on macOS/Linux:

```bash
./gradlew run
```

- on Windows:

```bash
.\gradlew.bat run
```

#### Running the unit tests (Not implemented yet)

- on macOS/Linux:

```bash
./gradlew test
```

- on Windows:

```bash
.\gradlew.bat test
```


### With an IDE
Open this repository as a Gradle project.

Then to start the application, run the method [TestLauncher.main](desktop/src/it/unibz/inf/pp/clash/TestLauncher.java)
  (in your running configuration, you may need to specify `assets` as the Java working directory, as LibGDX performs some strange black magic...).

On macOS, you may need to add `-XstartOnFirstThread` as a JVM option (Apple Silicon requires the graphics to run on the main thread).

## Design

### Game snapshot and board

The project is designed around the notion of game _snapshot_.\, which can be thought of as a save state.
It contains information about the player stats and the entities on the board.

Rules and structure of the board are implemented through the interface [Board](core/src/main/java/it/unibz/inf/pp/clash/model/snapshot/Board.java).\
A board is a rectangular grid.
Rows and columns on the board are indexed from left to right and top to bottom, with natural numbers, starting at index 0.
So the upper-left tile has index (0,0)


### Model-view-controller

In order to decouple the graphical interface from the mechanics of the game, the project (loosely) follows the [model-view-controller](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller) (MVC) pattern.\
This means that the code is partitioned into three components called
[model](core/src/main/java/it/unibz/inf/pp/clash/model/README.md),
[view](core/src/main/java/it/unibz/inf/pp/clash/view/README.md) and
[controller](core/src/main/java/it/unibz/inf/pp/clash/controller/README.md):

- The _model_ is the core of the application, implemented as an [Event Handler](core/src/main/java/it/unibz/inf/pp/clash/model/EventHandler.java).
  It keeps track of the state of the game (Snapshot) and updates it after each action.
  The model takes its input from the controller, and outputs drawing instructions to the view.
- The _controller_ registers the user actions (left and right click, hovering), and notifies the model of each action.
  It is implemented through [Listeners](core/src/main/java/it/unibz/inf/pp/clash/controller/listeners).
- The _view_ is in charge of drawing the game on screen. It takes its input from the model.
  It is not fancy, as it is implemented with an old imperative-like approach, rather than a declarative one.

## How to play
The game is played in turns. During their own turn, a player can do one of the following actions:

- Move of their units to a destination tile (LX click + Move button);
- Attack another unit (LX click + Attack button);
- Remove one of their units (RX click).

When a player is done, they can pass the turn with the Skip button.

Currently, the game board is more of a playground for testing interactions.
More rules would need to be implemented (decreasing attack countdown, attacking the opponent, etc...).


## Lessons on a more technical level
Many lessons had to be learnt from a development viewpoint:
- The complexity of a Gradle project, especially one focused on LibGDX -> learning how the various components interact with each other;
- Understanding the code written by another developer, i.e., what structures and functions they used;
- the importance (and extensive, pre-existing usage) of the "super" keyword in OOP.


## Experiences learnt on the way

The biggest difficulties are always two:

- Understanding what was written by another developer, i.e., changing your own mental framework to that of another person;
- Teamwork, i.e., sharing thoughts and opinions with other people, so that work might be collaborative. 


## About the team

Three team members:

- Boutaj Fatima Ezahra (BouFatimaunibz). Added "Tigre" unit class, and "Boardprova".
- Kyeremeth Nana (NKRMH). Added "Hare" and "Chef" unit classes.
- Antonio Piscitelli (Tonio-V98T) (APiscitelli). 
Because of an early mistake (and a non-collaborating Git Bash), I pushed commits with both my GitHub account (Tonio-V98T),
and a local account (APiscitelli). Implemented graphics (under view and assets), player input (new listeners),
basic game logic (snapshot, board, event handler), and the desktop launcher. Cleaned some code along the way, and changed some other stuff (as reflected in the various commits and PRs). 

This project is still in Beta, and will be submitted nonetheless.
However, I wish this would have been a _team_ project, not a solo one...
To this day, I am still not aware why the other members silently decided to abandon the project at its inception.