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

import wjd.amb.view.ICanvas;
import wjd.phage.level.Concentration;
import wjd.phage.level.Tile;

/**
 *
 * @author wdyce
 * @since Nov 24, 2012
 */
public class ConcentrationBrush extends ABrush
{
  /* CONSTANTS */
  private static final float PAINT_SPEED = 0.1f;
  
  /* ATTRIBUTES */
  
  private int type_i = 0;
  private Concentration.EType[] types = Concentration.EType.values();
  
  /* METHODS */
  public ConcentrationBrush()
  {
    super(true);
  }
  
  /* IMPLEMENTS -- ABRUSH */
  @Override
  public void paint(Tile target)
  {
    if(target.getType() == Tile.ETerrain.FLOOR)
      target.getConcentration(types[type_i]).tryDeposit(PAINT_SPEED);
  }
  
  @Override
  public void erase(Tile target)
  {
    target.getConcentration(types[type_i]).empty();
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
    canvas.box(coverage, false);
  }
}
