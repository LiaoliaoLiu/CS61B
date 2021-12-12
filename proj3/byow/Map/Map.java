package byow.Map;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

// TODO: String to integers. checked
// TODO: Pseudo random generator. checked
// TODO: Make hallways. checked
public class Map {
    private ArrayList<Rectangle> regions;
    private ArrayList<Rectangle> rooms;
    public static int width = 80;
    public static int height = 50;
    public TETile[][] world;
    public TERenderer ter;
    public Random dice;

    public Map(String input) {
        world = new TETile[width][height];
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                world[x][y] = Tileset.WALL;
            }
        }

        ter = new TERenderer();
        ter.initialize(width, height);
        ter.renderFrame(world);
        dice = new Random(Integer.decode(input));

        generateRooms();
        sortRooms();
        addHallways();
    }

    private void generateRooms() {
        regions = new ArrayList<Rectangle>();
        rooms = new ArrayList<Rectangle>();
        regions.add(new Rectangle(2, 2, width - 5, height - 5));
        Rectangle firstRoom = regions.get(0);
        addSubRegions(firstRoom);

        for (int i = 0; i < 240; i++) {
            Rectangle region = getRandomRegion();
            Rectangle candidate = getRandomSubRoom(region);

            if (isPossible(candidate)) {
                applyRoomToMap(candidate);
                addSubRegions(region);
                rooms.add(candidate);
                captureFrame(100);
            }
        }
    }

    private void captureFrame(int interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ter.renderFrame(world);
    }

    private void sortRooms() {
        Collections.sort(rooms, (Rectangle r1, Rectangle r2) -> {
            if (r1.x1 < r2.x1) {
                return -1;
            } else if (r1.x1 > r2.x1) {
                return 1;
            } else {
                return 0;
            }
        });
    }

    private void addHallways() {
        for (int i = 0; i < rooms.size() - 1; i++) {
            Rectangle room = rooms.get(i);
            Rectangle next_room = rooms.get(i + 1);
            int start_x = room.x1 + (generateRandomInt(Math.abs(room.x1 - room.x2)));
            int start_y = room.y1 + (generateRandomInt(Math.abs(room.y1 - room.y2)));
            int end_x = next_room.x1 + (generateRandomInt(Math.abs(next_room.x1 - next_room.x2)));
            int end_y = next_room.y1 + (generateRandomInt(Math.abs(next_room.y1 - next_room.y2)));
            drawCorridor(start_x, start_y, end_x, end_y);
            captureFrame(100);
        }
    }

    private void drawCorridor(int x1, int y1, int x2, int y2) {
        int x = x1;
        int y = y1;

        while (x != x2 || y != y2) {
            if (x < x2) {
                x += 1;
            } else if (x > x2) {
                x -= 1;
            } else if (y < y2) {
                y += 1;
            } else if (y > y2) {
                y -= 1;
            }

            world[x][y] = Tileset.FLOOR;
        }
    }

    private void addSubRegions(Rectangle room) {
        int width = Math.abs(room.x1 - room.x2);
        int height = Math.abs(room.y1 - room.y2);
        int half_width = Math.max(width / 2, 1);
        int half_height = Math.max(height / 2, 1);

        regions.add(new Rectangle(room.x1, room.y1, half_width, half_height));
        regions.add(new Rectangle(room.x1, room.y1 + half_height, half_width, half_height));
        regions.add(new Rectangle(room.x1 + half_width, room.y1, half_width, half_height));
        regions.add(new Rectangle(room.x1 + half_width, room.y1 + half_height, half_width, half_height));
        regions.remove(room);
    }

    private Rectangle getRandomRegion() {
        if (regions.size() == 1) {
            return regions.get(0);
        }

        return regions.get(generateRandomInt(regions.size()));
    }

    private Rectangle getRandomSubRoom(Rectangle room) {
        int room_width = Math.abs(room.x1 - room.x2);
        int room_height = Math.abs(room.y1 - room.y2);
        Rectangle result = new Rectangle(room.x1, room.y1, room_width, room_height);

        int w = Math.max(3, generateRandomInt(Math.min(room_width + 1, 10)) + 1);
        int h = Math.max(3, generateRandomInt(Math.min(room_height + 1, 10)) + 1);

        result.x1 += generateRandomInt(6);
        result.y1 += generateRandomInt(6);
        result.x2 = result.x1 + w;
        result.y2 = result.y1 + h;

        return result;
    }

    private int generateRandomInt(int bound) {
        return dice.nextInt(bound);
    }

    private boolean isPossible(Rectangle room) {
        Rectangle expanded = new Rectangle(room.x1, room.y1, room.x2 - room.x1, room.y2 - room.y1);
        expanded.x1 -= 2;
        expanded.x2 += 2;
        expanded.y1 -= 2;
        expanded.y2 += 2;

        boolean can_build = true;

        for (int y = expanded.y1; y <= expanded.y2; y++) {
            for (int x = expanded.x1; x <= expanded.x2; x++) {
                if (x > width - 2) {
                    can_build = false;
                }
                if (y > height - 2) {
                    can_build = false;
                }
                if (x < 1) {
                    can_build = false;
                }
                if (y < 1) {
                    can_build = false;
                }
                if (can_build) {
                    if (!world[x][y].equals(Tileset.WALL)) {
                        return false;
                    }
                }
            }
        }

        return can_build;
    }

    private void applyRoomToMap(Rectangle room) {
        for (int y = room.y1; y <= room.y2; y++) {
            for (int x = room.x1; x <= room.x2; x++) {
                world[x][y] = Tileset.FLOOR;
            }
        }
    }

    public static void main(String[] args) {
        Map map = new Map("42");
    }
}
