/*
 Copyright (C) 2012 William James Dyce

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wjd.phage.editor;

import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.math.Rect;
import wjd.phage.level.Tile;

/**
 *
 * @author wdyce
 * @since Nov 2, 2012
 */
public class TerrainBrush extends ABrush
{
  /* CONSTANTS */
  private static final Colour FLOOR_COLOUR = new Colour(1.0f, 0.0f, 0.0f, 0.5f);
  private static final Colour WALL_COLOUR = new Colour(0.0f, 0.0f, 1.0f, 0.5f);
  
  /* ATTRIBUTES */
  private Tile.EType colour = Tile.EType.FLOOR;

  /* METHODS */
  
  // constructors
  TerrainBrush()
  {
  }
  
  /* IMPLEMENTS -- IBRUSH */
  @Override
  public void paint(Tile target)
  {
    target.setType(colour);
  }

  @Override
  public void changeColour()
  {
    switch(colour)
    {
      case FLOOR:
        colour = Tile.EType.WALL;
        break;

      case WALL:
        colour = Tile.EType.FLOOR;
        break;
    }
  }
  
  /* IMPLEMENTS -- IVISIBLE */

  private static Rect stamp = new Rect(Tile.SIZE);
  @Override
  public void render(ICanvas canvas)
  {
    switch(colour)
    {
      case FLOOR:
        canvas.setColour(FLOOR_COLOUR);
      break;

      case WALL:
        canvas.setColour(WALL_COLOUR);
      break;
    }
    System.out.println("coverage="+coverage);
    canvas.box(coverage, true);
    canvas.setColour(Colour.BLACK);
    canvas.box(coverage, false);
  }
}
