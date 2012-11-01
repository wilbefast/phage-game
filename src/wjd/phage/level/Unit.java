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
package wjd.phage.level;

import wjd.phage.level.Tile;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;
import wjd.math.V2;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class Unit implements IVisible
{
  /* ATTRIBUTES */
  private Tile tile;
  private boolean selected = false;
  
  /* METHODS */

  // constructors
  public Unit(Tile tile)
  {
    this.tile = tile;
  }

  // accessors
  public boolean isSelected() { return selected; }

  // mutators
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }
  
  /* IMPLEMENTS -- IVISIBLE */
  private static V2 stamp_pos = new V2();
  @Override
  public void render(ICanvas canvas)
  {
    canvas.setColour(selected ? Colour.WHITE : Colour.BLACK);
    
    stamp_pos.reset(tile.getPosition()).scale(Tile.SIZE).add(Tile.HSIZE);
    canvas.circle(stamp_pos, Tile.SIZE.x()/2);
  }
}
