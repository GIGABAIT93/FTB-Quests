package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenBase;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenPart;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * @author LatvianModder
 */
public class BlockProgressScreen extends BlockWithHorizontalFacing
{
	public BlockProgressScreen()
	{
		super(Material.IRON, MapColor.BLACK);
		setCreativeTab(FTBQuests.TAB);
		setHardness(0.3F);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileProgressScreenCore();
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		items.add(new ItemStack(this));

		for (int i = 1; i <= 4; i++)
		{
			ItemStack stack = new ItemStack(this);
			stack.setTagInfo("Size", new NBTTagByte((byte) i));
			items.add(stack);
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return Item.getItemFromBlock(FTBQuestsItems.SCREEN);
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		ItemStack stack = new ItemStack(FTBQuestsItems.PROGRESS_SCREEN);

		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenBase)
		{
			TileProgressScreenCore screen = ((TileProgressScreenBase) tileEntity).getScreen();

			if (screen != null)
			{
				screen.writeToPickBlock(stack);
			}
		}

		return stack;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (facing != state.getValue(FACING))
		{
			return false;
		}

		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenBase)
		{
			TileProgressScreenBase base = (TileProgressScreenBase) tileEntity;
			TileProgressScreenCore screen = base.getScreen();

			if (screen != null)
			{
				if (player instanceof EntityPlayerMP)
				{
					screen.onClicked((EntityPlayerMP) player, BlockScreen.getClickX(facing, base.getOffsetX(), base.getOffsetZ(), hitX, hitZ, screen.size), BlockScreen.getClickY(base.getOffsetY(), hitY, screen.size));
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenCore)
		{
			TileProgressScreenCore screen = (TileProgressScreenCore) tileEntity;
			screen.readFromItem(stack);

			if (screen.team.isEmpty() && placer instanceof EntityPlayerMP)
			{
				screen.team = FTBLibAPI.getTeam(placer.getUniqueID());
			}

			screen.facing = state.getValue(FACING);

			if (screen.size > 0)
			{
				IBlockState state1 = FTBQuestsItems.PROGRESS_SCREEN_PART.getDefaultState().withProperty(FACING, screen.getFacing());

				boolean xaxis = screen.facing.getAxis() == EnumFacing.Axis.X;

				for (int y = 0; y < screen.size * 2 + 1; y++)
				{
					for (int x = -screen.size; x <= screen.size; x++)
					{
						if (x != 0 || y != 0)
						{
							int offX = xaxis ? 0 : x;
							int offZ = xaxis ? x : 0;
							world.setBlockToAir(new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ));
						}
					}
				}

				for (int y = 0; y < screen.size * 2 + 1; y++)
				{
					for (int x = -screen.size; x <= screen.size; x++)
					{
						if (x != 0 || y != 0)
						{
							int offX = xaxis ? 0 : x;
							int offZ = xaxis ? x : 0;
							BlockPos pos1 = new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ);
							world.setBlockState(pos1, state1);

							TileEntity tileEntity1 = world.getTileEntity(pos1);

							if (tileEntity1 instanceof TileProgressScreenPart)
							{
								((TileProgressScreenPart) tileEntity1).setOffset(offX, y, offZ);
							}
						}
					}
				}
			}
		}
	}

	@Override
	@Nullable
	public String getHarvestTool(IBlockState state)
	{
		return null;
	}

	@Override
	public int getHarvestLevel(IBlockState state)
	{
		return -1;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenCore)
		{
			TileProgressScreenCore screen = (TileProgressScreenCore) tileEntity;

			if (screen.size > 0)
			{
				BlockScreen.BREAKING_SCREEN = true;
				boolean xaxis = state.getValue(FACING).getAxis() == EnumFacing.Axis.X;

				for (int y = 0; y < screen.size * 2 + 1; y++)
				{
					for (int x = -screen.size; x <= screen.size; x++)
					{
						if (x != 0 || y != 0)
						{
							int offX = xaxis ? 0 : x;
							int offZ = xaxis ? x : 0;
							BlockPos pos1 = new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ);
							IBlockState state1 = world.getBlockState(pos1);

							if (state1.getBlock() == FTBQuestsItems.PROGRESS_SCREEN_PART)
							{
								world.setBlockToAir(pos1);
							}
						}
					}
				}

				BlockScreen.BREAKING_SCREEN = false;
			}
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	@Deprecated
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenBase)
		{
			TileProgressScreenCore screen = ((TileProgressScreenBase) tileEntity).getScreen();

			if (screen != null)
			{
				return BlockScreen.getScreenAABB(screen.getPos(), screen.getFacing(), screen.size);
			}
		}

		return new AxisAlignedBB(0D, -1D, 0D, 0D, -1D, 0D);
	}

	@Override
	@Deprecated
	public float getBlockHardness(IBlockState state, World world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenBase)
		{
			TileProgressScreenCore core = ((TileProgressScreenBase) tileEntity).getScreen();

			if (core != null && core.indestructible)
			{
				return -1F;
			}
		}

		return super.getBlockHardness(state, world, pos);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenBase)
		{
			TileProgressScreenCore core = ((TileProgressScreenBase) tileEntity).getScreen();

			if (core != null && core.indestructible)
			{
				return Float.MAX_VALUE;
			}
		}

		return super.getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	@Deprecated
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileProgressScreenBase)
		{
			TileProgressScreenCore core = ((TileProgressScreenBase) tileEntity).getScreen();

			if (core != null && core.skin != BlockUtils.AIR_STATE)
			{
				return core.skin;
			}
		}

		return state;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		if (world == null || !ClientQuestFile.exists())
		{
			return;
		}

		NBTTagCompound nbt = stack.getTagCompound();
		int size = nbt == null ? 0 : nbt.getByte("Size");
		String team = nbt == null ? "" : nbt.getString("Team");

		if (team.isEmpty())
		{
			team = ClientQuestFile.existsWithTeam() ? ClientQuestFile.INSTANCE.self.getTeamID() : "";
		}

		tooltip.add(I18n.format("tile.ftbquests.screen.size") + ": " + TextFormatting.GOLD + (1 + size * 2) + " x " + (1 + size * 2));
		tooltip.add(I18n.format("ftbquests.team") + ": " + TextFormatting.DARK_GREEN + team);

		ProgressingQuestObject object = ClientQuestFile.INSTANCE.getProgressing(nbt == null ? "" : nbt.getString("Object"));

		if (object != null)
		{
			tooltip.add(StringUtils.color(object.getDisplayName(), TextFormatting.YELLOW).getFormattedText());
		}
	}
}