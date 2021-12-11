package byow.Map;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

// TODO: Pseudo random generator
// TODO:
public class Map {
    private ArrayList<Rectangle> regions;
    private ArrayList<Rectangle> rooms;
    public static int width = 80;
    public static int height = 50;
    public TETile[][] world;

    public Map(){
        world = new TETile[width][height];
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                world[x][y] = Tileset.WALL;
            }
        }

        TERenderer ter = new TERenderer();
        ter.initialize(width, height);
        ter.renderFrame(world);

        regions = new ArrayList<Rectangle>();
        rooms = new ArrayList<Rectangle>();
        regions.add(new Rectangle(2, 2, width - 5, height - 5));
        Rectangle firstRoom = regions.get(0);
        addSubRegions(firstRoom);

        for (int i = 0; i < 240; i++) {
            Rectangle region = getRandomRegion();
            Rectangle candidate = getRandomSubRoom(region);

            if (is_possible(candidate)) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                applyRoomToMap(candidate);
                addSubRegions(region);
                boolean bug = isOverlap(candidate);
                rooms.add(candidate);
                ter.renderFrame(world);
            }
        }
    }

    public boolean isOverlap(Rectangle room) {
        boolean overlap = false;
        for (Rectangle r : rooms) {
            overlap = room.intersect(r);
        }

        return overlap;
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
    }

    private Rectangle getRandomRegion() {
        if (regions.size() == 1) {
            return regions.get(0);
        }

        return regions.get(ThreadLocalRandom.current().nextInt(0, regions.size()));
    }

    private Rectangle getRandomSubRoom(Rectangle room) {
        int room_width = Math.abs(room.x1 - room.x2);
        int room_height = Math.abs(room.y1 - room.y2);
        Rectangle result = new Rectangle(room.x1, room.y1, room_width, room_height);

        int w = Math.max(3, generateRandomInt(1, Math.min(room_width+1, 10))+1);
        int h = Math.max(3, generateRandomInt(1, Math.min(room_height+1, 10))+1);

        result.x1 += generateRandomInt(1, 6) - 1;
        result.y1 += generateRandomInt(1, 6) - 1;
        result.x2 = result.x1 + w;
        result.y2 = result.y1 + h;

        return result;
    }

    private int generateRandomInt(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

    private boolean is_possible(Rectangle room) {
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
                if (x< 1) {
                    can_build = false;
                }
                if (y< 1) {
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
        TERenderer ter = new TERenderer();
        ter.initialize(width, height);
        Map map = new Map();
        ter.renderFrame(map.world);
    }
}
