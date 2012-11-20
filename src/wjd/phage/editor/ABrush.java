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

import wjd.amb.view.IVisible;
import wjd.math.Rect;
import wjd.math.V2;
import wjd.phage.level.Tile;
import wjd.phage.level.TileGrid;

/**
 *
 * @author wdyce
 * @since Nov 2, 2012
 */
public abstract class ABrush implements IVisible
{
  /* ATTRIBUTES */
  protected Rect coverage = new Rect();
  
  /* METHODS */
  public void setPosition(V2 position)
  {
    coverage.centrePos(position);
  }
  public void setSize(float size)
  {
    coverage.size(Tile.SIZE).scale(size);
  }
  public void paint(TileGrid grid)
  {
    TileGrid target_field = grid.createSubGrid(coverage);
    for(Tile target : target_field)
      paint(target);
  }
  
  /* INTERFACE */
  public abstract void paint(Tile target);
  public abstract void changeColour();
}


