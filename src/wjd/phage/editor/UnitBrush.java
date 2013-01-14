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
import wjd.phage.level.Tile;
import wjd.phage.unit.Unit;

/**
 *
 * @author wdyce
 * @since Nov 2, 2012
 */
public class UnitBrush extends ABrush
{
  /* ATTRIBUTES */
  private int type_i = 0;
  private Unit.Type[] types = Unit.Type.values();
  
  
  
  /* METHODS */
  public UnitBrush()
  {
    super(false);
  }
  
  /* IMPLEMENTS -- ABRUSH */
  
  @Override
  public void paint(Tile target)
  {
    // create unit
    if(target.getType() != Tile.EType.WALL)
      target.setUnit(Unit.fromType(types[type_i], target));
  }
  
  @Override
  public void erase(Tile target)
  {
    // delete unit
    target.setUnit(null);
  }

  @Override
  public void changeColour()
  {
    type_i = (type_i + 1) % types.length;
  }
  
  /* IMPLEMENTS -- IVISIBLE */

  @Override
  public void render(ICanvas canvas)
  {
    canvas.setColour(types[type_i].colour);
    canvas.circle(coverage.getCentre(), Tile.HSIZE.x, false);
  }
}
